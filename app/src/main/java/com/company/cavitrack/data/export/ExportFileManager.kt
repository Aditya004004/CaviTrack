package com.company.cavitrack.data.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.company.cavitrack.domain.export.ExportFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportFileManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {
        /**
         * Sanitizes user-supplied filenames to eliminate directory traversal attacks (CWE-22).
         * Replaces path characters (.., /, \) and non-alphanumeric characters (except _ and -),
         * truncating to 64 characters max.
         */
        fun sanitizeFileName(name: String): String {
            if (name.isBlank()) return "Export_${System.currentTimeMillis()}"
            val cleanName = name
                .replace("\\", "_")
                .replace("/", "_")
                .replace("..", "_")
                .replace(Regex("[^a-zA-Z0-9_\\-\\.]"), "_")
                .replace(Regex("_+"), "_")
                .trim('_', '.')
            return if (cleanName.isBlank()) "Export_${System.currentTimeMillis()}" else cleanName.take(64)
        }
    }

    suspend fun saveExportToCache(
        fileName: String,
        format: ExportFormat,
        dataBytes: ByteArray
    ): Pair<File, Uri> = withContext(Dispatchers.IO) {
        val safeBaseName = sanitizeFileName(fileName)
        val fullFileName = "$safeBaseName${format.extension}"

        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val file = File(exportDir, fullFileName)

        FileOutputStream(file).use { fos ->
            fos.write(dataBytes)
            fos.flush()
        }

        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        Pair(file, uri)
    }

    suspend fun saveExportToDownloads(
        fileName: String,
        format: ExportFormat,
        dataBytes: ByteArray
    ): Uri? = withContext(Dispatchers.IO) {
        val safeBaseName = sanitizeFileName(fileName)
        val fullFileName = "$safeBaseName${format.extension}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CaviTrack")
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { targetUri ->
                resolver.openOutputStream(targetUri)?.use { os ->
                    os.write(dataBytes)
                    os.flush()
                }
            }
            uri
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val caviTrackDir = File(downloadsDir, "CaviTrack").apply { if (!exists()) mkdirs() }
            val file = File(caviTrackDir, fullFileName)
            FileOutputStream(file).use { fos ->
                fos.write(dataBytes)
                fos.flush()
            }
            Uri.fromFile(file)
        }
    }

    fun createShareIntent(fileUri: Uri, mimeType: String, title: String = "Share Export"): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
