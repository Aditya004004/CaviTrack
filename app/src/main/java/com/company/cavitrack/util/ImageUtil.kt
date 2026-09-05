package com.company.cavitrack.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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

        // If the image already fits within the max dimension, avoid unnecessary recompression
        if (options.outHeight > 0 && options.outWidth > 0 &&
            options.outHeight <= maxDim && options.outWidth <= maxDim &&
            file.name.endsWith(".jpg", ignoreCase = true)
        ) {
            return@withContext file
        }

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

        val bitmap = try {
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (oom: OutOfMemoryError) {
            if (com.company.cavitrack.BuildConfig.DEBUG) {
                Log.e("ImageUtil", "Out of memory decoding image", oom)
            }
            null
        } ?: return@withContext null

        val orientation = try {
            val exif = android.media.ExifInterface(file.absolutePath)
            exif.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
        } catch (e: Exception) {
            android.media.ExifInterface.ORIENTATION_NORMAL
        }

        val rotationDegrees = when (orientation) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        var processedBitmap = bitmap
        if (rotationDegrees != 0f) {
            val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
            try {
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    processedBitmap = rotated
                }
            } catch (oom: OutOfMemoryError) {
                if (com.company.cavitrack.BuildConfig.DEBUG) {
                    Log.w("ImageUtil", "OOM rotating bitmap, falling back to unrotated", oom)
                }
            }
        }

        val parentDir = file.parentFile ?: return@withContext null
        val tempFile = File(parentDir, "downscaled_${System.currentTimeMillis()}_${file.name}")
        try {
            FileOutputStream(tempFile).use { out ->
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            if (tempFile.exists() && tempFile.length() > 0) {
                // Safe replacement: try atomic rename, fallback to overwrite copy
                if (!tempFile.renameTo(file)) {
                    tempFile.copyTo(file, overwrite = true)
                    tempFile.delete()
                }
            }
            file
        } catch (t: Throwable) {
            if (com.company.cavitrack.BuildConfig.DEBUG) {
                Log.e("ImageUtil", "Failed to downscale image", t)
            }
            if (tempFile.exists()) {
                tempFile.delete()
            }
            null
        } finally {
            processedBitmap.recycle()
        }
    }
}
