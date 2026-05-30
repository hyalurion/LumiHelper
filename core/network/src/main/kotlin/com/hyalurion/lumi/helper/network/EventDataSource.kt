package com.hyalurion.lumi.helper.network

import android.content.Context
import com.hyalurion.lumi.helper.model.GameEvent
import com.hyalurion.lumi.helper.model.EventType
import com.hyalurion.lumi.helper.model.HoYoLabNewsResponse
import com.hyalurion.lumi.helper.model.VersionInfo
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for game events.
 * Tries HoYoLab news API first, falls back to bundled JSON.
 */
@Singleton
class EventDataSource @Inject constructor(
    private val moshi: Moshi,
    @ApplicationContext private val context: Context,
) {
    // Lazy-initialized HoYoLab news API (no auth needed)
    private val newsApi: HoYoLabNewsApi by lazy {
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://bbs-api-os.hoyolab.com/")
            .client(
                okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(HoYoLabNewsApi::class.java)
    }

    // Game ID mapping for HoYoLab API
    private val gameIdMap = mapOf(
        "GI" to 1,
        "HSR" to 6,
        "ZZZ" to 8,
    )

    /**
     * Fetch events from HoYoLab news API for all supported games.
     * Returns a list of parsed GameEvent objects.
     */
    suspend fun fetchEventsFromApi(): Result<List<GameEvent>> = withContext(Dispatchers.IO) {
        try {
            val allEvents = gameIdMap.entries.map { (game, gids) ->
                async {
                    try {
                        fetchEventsForGame(game, gids)
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll().flatten()

            if (allEvents.isEmpty()) {
                Result.failure(Exception("No events fetched from API"))
            } else {
                Result.success(allEvents)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch events for a single game from HoYoLab news.
     */
    private suspend fun fetchEventsForGame(game: String, gids: Int): List<GameEvent> {
        // Fetch activity announcements (type=2) and info posts (type=1)
        val responses = listOf(
            newsApi.getNewsList(gids = gids, type = 2, pageSize = 20),
            newsApi.getNewsList(gids = gids, type = 1, pageSize = 10),
        )

        return responses.flatMap { response ->
            parseNewsToEvents(response, game)
        }
    }

    /**
     * Parse HoYoLab news posts into GameEvent objects.
     * Tries to extract event dates from post content.
     */
    private fun parseNewsToEvents(response: HoYoLabNewsResponse, game: String): List<GameEvent> {
        val posts = response.data?.list ?: return emptyList()

        return posts.mapNotNull { post ->
            // Use the post creation time as a reference
            val createdAt = post.createdAt
            // Posts from HoYoLab don't always have structured event dates,
            // so we create event entries from announcements
            GameEvent(
                id = "${game}_${post.postId}",
                name = post.subject,
                description = post.content.take(200),
                startTime = createdAt.toString(),
                endTime = (createdAt + 14 * 24 * 3600).toString(), // Assume 2-week duration
                type = guessEventType(post.subject),
                game = game,
                iconUrl = post.cover.ifBlank { null },
                wikiUrl = "https://www.hoyolab.com/article/${post.postId}",
                bannerUrl = post.images.firstOrNull(),
            )
        }
    }

    /**
     * Guess event type from post title.
     */
    private fun guessEventType(title: String): EventType {
        val lower = title.lowercase()
        return when {
            lower.contains("维护") || lower.contains("maintenance") -> EventType.MAINTENANCE
            lower.contains("版本") || lower.contains("version") || lower.contains("更新") -> EventType.VERSION_UPDATE
            lower.contains("卡池") || lower.contains("祈愿") || lower.contains("banner") || lower.contains("warp") -> EventType.BANNER
            lower.contains("纪行") || lower.contains("battle pass") || lower.contains("通行证") -> EventType.BATTLE_PASS
            lower.contains("网页") || lower.contains("web event") -> EventType.WEB_EVENT
            lower.contains("活动") || lower.contains("event") -> EventType.LIMITED_TIME
            else -> EventType.OTHER
        }
    }

    /**
     * Load bundled version info from assets.
     */
    suspend fun loadBundledVersionInfo(): List<VersionInfo> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open("bundled_events.json").bufferedReader().use { it.readText() }
            val adapter = moshi.adapter(BundledEventsData::class.java)
            val data = adapter.fromJson(json)
            data?.version_info ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Load bundled events from assets.
     */
    suspend fun loadBundledEvents(): List<GameEvent> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open("bundled_events.json").bufferedReader().use { it.readText() }
            val adapter = moshi.adapter(BundledEventsData::class.java)
            val data = adapter.fromJson(json)
            data?.events ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}

/**
 * Internal model for parsing bundled_events.json.
 */
@JsonClass(generateAdapter = true)
internal data class BundledEventsData(
    val version_info: List<VersionInfo> = emptyList(),
    val events: List<GameEvent> = emptyList(),
)
