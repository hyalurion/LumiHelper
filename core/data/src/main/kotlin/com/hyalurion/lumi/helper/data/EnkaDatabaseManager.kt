package com.hyalurion.lumi.helper.data

import android.content.Context
import com.hyalurion.lumi.helper.model.EnkaArtifactMeta
import com.hyalurion.lumi.helper.model.EnkaCharacterMeta
import com.hyalurion.lumi.helper.model.EnkaDatabase
import com.hyalurion.lumi.helper.model.EnkaProfilePicture
import com.hyalurion.lumi.helper.model.EnkaWeaponMeta
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enka database manager.
 * Downloads and caches character/weapon/artifact metadata from Enka.Network.
 * Ported from PizzaHelperUnited's EnkaKit.
 */
@Singleton
class EnkaDatabaseManager(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi,
) {
    private val cacheDir: File
        get() = File(context.filesDir, "enkadb").also { it.mkdirs() }

    private var cachedDb: EnkaDatabase? = null

    companion object {
        private const val ENKA_DB_URL = "https://raw.githubusercontent.com/pizza-studio/EnkaDBGenerator/main/Sources/EnkaDBFiles/Resources/Specimen/"
        private const val CACHE_FILE = "enkadb_cache.json"
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val DEFAULT_LANG = "chs"
    }

    /**
     * Get the Enka database, downloading if necessary.
     */
    suspend fun getDatabase(): EnkaDatabase = withContext(Dispatchers.IO) {
        cachedDb?.let { return@withContext it }

        val cacheFile = File(cacheDir, CACHE_FILE)
        val now = System.currentTimeMillis()

        // Try to load from cache
        if (cacheFile.exists()) {
            val lastModified = cacheFile.lastModified()
            if (now - lastModified < CACHE_TTL_MS) {
                try {
                    val json = cacheFile.readText()
                    val db = moshi.adapter(EnkaDatabase::class.java).fromJson(json)
                    if (db != null) {
                        cachedDb = db
                        return@withContext db
                    }
                } catch (_: Exception) { }
            }
        }

        // Download fresh data
        try {
            val db = downloadDatabase()
            if (db != null) {
                val json = moshi.adapter(EnkaDatabase::class.java).toJson(db)
                cacheFile.writeText(json)
                cachedDb = db
                return@withContext db
            }
        } catch (_: Exception) { }

        // Fallback: try stale cache
        if (cacheFile.exists()) {
            try {
                val json = cacheFile.readText()
                val db = moshi.adapter(EnkaDatabase::class.java).fromJson(json)
                if (db != null) {
                    cachedDb = db
                    return@withContext db
                }
            } catch (_: Exception) { }
        }

        // Return empty database as last resort
        EnkaDatabase()
    }

    /**
     * Get character name by avatar ID.
     */
    suspend fun getCharacterName(avatarId: Int): String? {
        val db = getDatabase()
        return db.characters[avatarId.toString()]?.name
    }

    /**
     * Get weapon name by item ID.
     */
    suspend fun getWeaponName(itemId: Int): String? {
        val db = getDatabase()
        return db.weapons[itemId.toString()]?.name
    }

    /**
     * Get artifact name by item ID.
     */
    suspend fun getArtifactName(itemId: Int): String? {
        val db = getDatabase()
        return db.artifacts[itemId.toString()]?.name
    }

    /**
     * Download database from Enka.Network resources.
     */
    private suspend fun downloadDatabase(): EnkaDatabase? = withContext(Dispatchers.IO) {
        try {
            // Download loc tables (localization) first
            val rawLocTables = downloadRawLocTables() ?: return@withContext null
            val locTable = rawLocTables[DEFAULT_LANG] ?: rawLocTables.values.firstOrNull() ?: emptyMap()

            // Download GI data
            val characters = downloadCharacters() ?: emptyMap()
            val weapons = downloadWeapons() ?: emptyMap()
            val artifacts = downloadArtifacts() ?: emptyMap()
            val profilePictures = downloadProfilePictures() ?: emptyMap()

            // Process characters to resolve names using locTable
            val processedCharacters = characters.mapValues { (_, char) ->
                char.copy(
                    name = char.nameTextMapHash?.let { hash ->
                        locTable[hash.toString()]
                    }
                )
            }

            val processedWeapons = weapons.mapValues { (_, weapon) ->
                weapon.copy(
                    name = weapon.nameTextMapHash?.let { hash ->
                        locTable[hash.toString()]
                    }
                )
            }

            val processedArtifacts = artifacts.mapValues { (_, artifact) ->
                artifact.copy(
                    name = artifact.nameTextMapHash?.let { hash ->
                        locTable[hash.toString()]
                    }
                )
            }

            EnkaDatabase(
                locTable = locTable,
                characters = processedCharacters,
                weapons = processedWeapons,
                artifacts = processedArtifacts,
                profilePictures = profilePictures,
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun downloadCharacters(): Map<String, EnkaCharacterMeta>? {
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, EnkaCharacterMeta::class.java)
            downloadJson("GI/characters.json", type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun downloadWeapons(): Map<String, EnkaWeaponMeta>? {
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, EnkaWeaponMeta::class.java)
            downloadJson("GI/weapons.json", type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun downloadArtifacts(): Map<String, EnkaArtifactMeta>? {
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, EnkaArtifactMeta::class.java)
            downloadJson("GI/reliquaries.json", type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun downloadProfilePictures(): Map<String, EnkaProfilePicture>? {
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, EnkaProfilePicture::class.java)
            downloadJson("GI/pfps.json", type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun <T> downloadJson(path: String, type: java.lang.reflect.Type): T? {
        return try {
            val url = URL("${ENKA_DB_URL}$path")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().readText()
                val adapter = moshi.adapter<T>(type)
                adapter.fromJson(json)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Download raw localization tables.
     */
    private fun downloadRawLocTables(): Map<String, Map<String, String>>? {
        return try {
            // Try GI loc table first
            val url = URL("${ENKA_DB_URL}GI/loc.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().readText()
                val type = Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
                val adapter = moshi.adapter<Map<String, Map<String, String>>>(type)
                adapter.fromJson(json)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clear the database cache.
     */
    fun clearCache() {
        File(cacheDir, CACHE_FILE).delete()
        cachedDb = null
    }
}
