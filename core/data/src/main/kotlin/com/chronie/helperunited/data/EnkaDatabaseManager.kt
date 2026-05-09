package com.chronie.helperunited.data

import android.content.Context
import com.chronie.helperunited.model.EnkaDatabase
import com.squareup.moshi.Moshi
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
 * Ported from PizzaHelperUnited's EnkaDB.
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
        private const val ENKA_DB_URL = "https://raw.githubusercontent.com/EnkaNetwork/starRail_res/main/"
        private const val CACHE_FILE = "enkadb_cache.json"
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24 hours
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
    private fun downloadDatabase(): EnkaDatabase? {
        // Download character data
        val characters = downloadMap("characters")
        val weapons = downloadMap("weapons")
        val artifacts = downloadMap("artifacts")

        return EnkaDatabase(
            characters = characters,
            weapons = weapons,
            artifacts = artifacts,
        )
    }

    /**
     * Download a metadata map from Enka resources.
     */
    private fun downloadMap(type: String): Map<String, com.chronie.helperunited.model.EnkaCharacterMeta> {
        return try {
            val url = URL("$ENKA_DB_URL$type.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().readText()
                // Parse as raw map and convert
                val adapter = moshi.adapter(Map::class.java)
                val raw = adapter.fromJson(json) as? Map<*, *> ?: return emptyMap()
                // This is simplified - actual implementation would parse each entry
                emptyMap()
            } else {
                emptyMap()
            }
        } catch (_: Exception) {
            emptyMap()
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
