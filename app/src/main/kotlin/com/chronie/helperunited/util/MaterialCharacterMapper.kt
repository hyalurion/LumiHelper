package com.chronie.helperunited.util

import android.content.Context
import org.json.JSONObject
import java.io.IOException

/**
 * Maps material domains to characters that need those materials.
 * Loads mapping data from bundled character_materials.json asset.
 */
object MaterialCharacterMapper {

    private var cachedMappings: MaterialMappings? = null

    /**
     * Load character-material mappings from the JSON asset file.
     */
    fun loadMappings(context: Context): MaterialMappings {
        cachedMappings?.let { return it }

        return try {
            val jsonString = context.assets.open("character_materials.json")
                .bufferedReader()
                .use { it.readText() }
            val json = JSONObject(jsonString)
            val mappings = parseMappings(json)
            cachedMappings = mappings
            mappings
        } catch (e: IOException) {
            e.printStackTrace()
            MaterialMappings(emptyMap(), emptyMap())
        }
    }

    private fun parseMappings(json: JSONObject): MaterialMappings {
        val talentDomains = mutableMapOf<String, DomainCharacterMapping>()
        val weaponDomains = mutableMapOf<String, DomainCharacterMapping>()

        // Parse talent domains
        val talentJson = json.optJSONObject("talent_domains")
        talentJson?.keys()?.forEach { domainName ->
            val domainJson = talentJson.getJSONObject(domainName)
            val region = domainJson.getString("region")
            val days = mutableListOf<Int>()
            val daysArray = domainJson.getJSONArray("days")
            for (i in 0 until daysArray.length()) {
                days.add(daysArray.getInt(i))
            }
            val materialMap = mutableMapOf<String, List<String>>()
            val materialsJson = domainJson.getJSONObject("materials")
            materialsJson.keys()?.forEach { materialName ->
                val charsArray = materialsJson.getJSONArray(materialName)
                val chars = mutableListOf<String>()
                for (i in 0 until charsArray.length()) {
                    chars.add(charsArray.getString(i))
                }
                materialMap[materialName] = chars
            }
            talentDomains[domainName] = DomainCharacterMapping(domainName, region, days, materialMap)
        }

        // Parse weapon domains
        val weaponJson = json.optJSONObject("weapon_domains")
        weaponJson?.keys()?.forEach { domainName ->
            val domainJson = weaponJson.getJSONObject(domainName)
            val region = domainJson.getString("region")
            val days = mutableListOf<Int>()
            val daysArray = domainJson.getJSONArray("days")
            for (i in 0 until daysArray.length()) {
                days.add(daysArray.getInt(i))
            }
            val materialMap = mutableMapOf<String, List<String>>()
            val materialsJson = domainJson.getJSONObject("materials")
            materialsJson.keys()?.forEach { materialName ->
                val charsArray = materialsJson.getJSONArray(materialName)
                val chars = mutableListOf<String>()
                for (i in 0 until charsArray.length()) {
                    chars.add(charsArray.getString(i))
                }
                materialMap[materialName] = chars
            }
            weaponDomains[domainName] = DomainCharacterMapping(domainName, region, days, materialMap)
        }

        return MaterialMappings(talentDomains, weaponDomains)
    }
}

data class MaterialMappings(
    val talentDomains: Map<String, DomainCharacterMapping>,
    val weaponDomains: Map<String, DomainCharacterMapping>,
)

data class DomainCharacterMapping(
    val domainName: String,
    val region: String,
    val days: List<Int>,
    val materialCharacters: Map<String, List<String>>,
) {
    /**
     * Get all unique characters that use materials from this domain.
     */
    val allCharacters: List<String>
        get() = materialCharacters.values.flatten().distinct().sorted()
}
