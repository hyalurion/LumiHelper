package com.hyalurion.lumi.helper.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hyalurion.lumi.helper.model.UIGFExport
import com.hyalurion.lumi.helper.model.UIGFInfo
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UIGF import/export manager.
 * Handles file-based import/export of gacha records in UIGF v4 format.
 * Ported from PizzaHelperUnited's GachaKit export logic.
 */
@Singleton
class UIGFManager(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi,
    private val gachaRepository: GachaRepository,
) {
    /**
     * Export gacha records to UIGF JSON string.
     */
    suspend fun exportToJson(uid: String, game: String): String = withContext(Dispatchers.IO) {
        val export = gachaRepository.exportUIGF(uid, game)
        moshi.adapter(UIGFExport::class.java).indent("  ").toJson(export)
    }

    /**
     * Import gacha records from a JSON string.
     * @return Result with count of imported records
     */
    suspend fun importFromJson(json: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val adapter = moshi.adapter(UIGFExport::class.java)
            val export = adapter.fromJson(json)
                ?: return@withContext Result.failure(Exception("无效的 UIGF 格式"))

            gachaRepository.insertEntries(export.list)
            Result.success(export.list.size)
        } catch (e: Exception) {
            Result.failure(Exception("导入失败: ${e.message}"))
        }
    }

    /**
     * Import gacha records from a content URI (e.g., from file picker).
     */
    suspend fun importFromUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).readText()
            } ?: return@withContext Result.failure(Exception("无法读取文件"))

            importFromJson(json)
        } catch (e: Exception) {
            Result.failure(Exception("读取文件失败: ${e.message}"))
        }
    }

    /**
     * Create a share intent for exporting gacha records.
     */
    suspend fun createShareIntent(uid: String, game: String): Intent = withContext(Dispatchers.IO) {
        val json = exportToJson(uid, game)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, json)
            putExtra(Intent.EXTRA_SUBJECT, "UIGF Export - $uid ($game)")
        }
        Intent.createChooser(intent, "导出抽卡记录")
    }

    /**
     * Generate a default filename for export.
     */
    fun getExportFilename(uid: String, game: String): String {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return "uigf_${game}_${uid}_$now.json"
    }
}
