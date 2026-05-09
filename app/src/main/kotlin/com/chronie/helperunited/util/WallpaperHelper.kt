package com.chronie.helperunited.util

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.chronie.helperunited.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Helper for downloading character art and setting it as wallpaper.
 * Supports saving to gallery (MediaStore) and setting as device wallpaper.
 */
object WallpaperHelper {

    /**
     * Download image from URL and return a Bitmap.
     */
    private suspend fun downloadImage(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.doInput = true
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            connection.disconnect()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resize/crop bitmap to fit device screen dimensions.
     */
    private fun fitToScreen(context: Context, bitmap: Bitmap): Bitmap {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        // Calculate scale to fill screen while maintaining aspect ratio
        val scaleX = screenWidth.toFloat() / bitmapWidth
        val scaleY = screenHeight.toFloat() / bitmapHeight
        val scale = maxOf(scaleX, scaleY)

        val scaledWidth = (bitmapWidth * scale).toInt()
        val scaledHeight = (bitmapHeight * scale).toInt()

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true)

        // Center crop to screen size
        val xOffset = (scaledWidth - screenWidth) / 2
        val yOffset = (scaledHeight - screenHeight) / 2

        return try {
            Bitmap.createBitmap(scaledBitmap, xOffset.coerceAtLeast(0), yOffset.coerceAtLeast(0), screenWidth, screenHeight)
        } catch (e: Exception) {
            // Fallback: return scaled bitmap without crop
            scaledBitmap
        }
    }

    /**
     * Save bitmap to device gallery using MediaStore API.
     * Returns the Uri of the saved image, or null on failure.
     */
    suspend fun saveToGallery(context: Context, bitmap: Bitmap, filename: String): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HelperUnited")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(it, contentValues, null, null)
                    }
                }

                uri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    /**
     * Set bitmap as device wallpaper using WallpaperManager.
     * Supports both home screen and lock screen on API 24+.
     */
    suspend fun setAsWallpaper(context: Context, bitmap: Bitmap): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.setBitmap(bitmap)
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

    /**
     * Download image from URL, save to gallery, and optionally set as wallpaper.
     *
     * @param context Android context
     * @param imageUrl URL of the character art
     * @param characterName Used for the filename
     * @param setAsWallpaper Whether to also set as device wallpaper
     * @return true if the operation succeeded
     */
    suspend fun downloadAndSave(
        context: Context,
        imageUrl: String,
        characterName: String,
        setAsWallpaper: Boolean = false,
    ): WallpaperResult = withContext(Dispatchers.Main) {
        val bitmap = downloadImage(imageUrl)
        if (bitmap == null) {
            return@withContext WallpaperResult(
                success = false,
                errorMessage = context.getString(R.string.wallpaper_failed),
            )
        }

        val screenFittedBitmap = fitToScreen(context, bitmap)

        // Save to gallery
        val safeFilename = characterName.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fff_-]"), "_")
        val filename = "HU_${safeFilename}_${System.currentTimeMillis()}.png"
        val savedUri = saveToGallery(context, screenFittedBitmap, filename)

        if (savedUri == null) {
            return@withContext WallpaperResult(
                success = false,
                errorMessage = context.getString(R.string.wallpaper_failed),
            )
        }

        // Set as wallpaper if requested
        if (setAsWallpaper) {
            val wallpaperSet = setAsWallpaper(context, screenFittedBitmap)
            if (!wallpaperSet) {
                return@withContext WallpaperResult(
                    success = false,
                    savedUri = savedUri,
                    errorMessage = context.getString(R.string.wallpaper_failed),
                )
            }
        }

        WallpaperResult(
            success = true,
            savedUri = savedUri,
            isWallpaperSet = setAsWallpaper,
        )
    }
}

/**
 * Result of a wallpaper operation.
 */
data class WallpaperResult(
    val success: Boolean,
    val savedUri: Uri? = null,
    val isWallpaperSet: Boolean = false,
    val errorMessage: String? = null,
)
