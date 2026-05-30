package com.hyalurion.lumi.helper.network

import com.hyalurion.lumi.helper.model.EnkaResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Enka.Network API service for character showcase data.
 * Ported from PizzaHelperUnited's EnkaKit.
 */
interface EnkaNetworkApi {

    /**
     * Fetch character showcase data from Enka.Network.
     * @param uid The game UID to query
     * @return EnkaResponse containing player info and avatar details
     */
    @GET("api/uid/{uid}")
    suspend fun getShowcase(@Path("uid") uid: String): EnkaResponse
}

/**
 * Enka.Network asset URL builder.
 * Provides URLs for character/weapon/artifact icons.
 */
object EnkaAssetUrls {

    private const val BASE_URL = "https://enka.network/ui/"

    /**
     * Get the icon URL for a character or item.
     * @param iconPath The icon path from Enka database (e.g., "UI_AvatarIcon_Ambor")
     * @return Full URL to the icon
     */
    fun getIconUrl(iconPath: String): String {
        return "$BASE_URL$iconPath.png"
    }

    /**
     * Get the side icon URL for a character.
     * @param iconPath The side icon path from Enka database
     * @return Full URL to the side icon
     */
    fun getSideIconUrl(iconPath: String): String {
        return "$BASE_URL$iconPath.png"
    }

    /**
     * Get the profile picture URL.
     * @param iconPath The profile picture icon path
     * @return Full URL to the profile picture
     */
    fun getProfilePictureUrl(iconPath: String): String {
        return "$BASE_URL$iconPath.png"
    }
}
