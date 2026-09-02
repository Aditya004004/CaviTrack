package com.company.cavitrack.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageUtil {
    suspend fun downscaleImage(file: File, maxDim: Int = 1280): File? = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext null

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)

        var inSampleSize = 1
        if (options.outHeight > maxDim || options.outWidth > maxDim) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) > maxDim || (halfWidth / inSampleSize) > maxDim) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return@withContext null
        
        val tempFile = File(file.parentFile, "downscaled_${file.name}")
        try {
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            if (file.exists()) file.delete()
            tempFile.renameTo(file)
            file
        } catch (e: Exception) {
            if (com.company.cavitrack.BuildConfig.DEBUG) {
                android.util.Log.e("ImageUtil", "Failed to downscale image", e)
            }
            null
        } finally {
            bitmap.recycle()
        }
    }
}
