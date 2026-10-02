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

    suspend fun saveExportToCache(
        fileName: String,
        format: ExportFormat,
        dataBytes: ByteArray
    ): Pair<File, Uri> = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val fullFileName = if (fileName.endsWith(format.extension)) fileName else "$fileName${format.extension}"
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
        val fullFileName = if (fileName.endsWith(format.extension)) fileName else "$fileName${format.extension}"

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
