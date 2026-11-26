package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.model.BrandingSlot
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object BrandingImageStore {

    private const val DIR_NAME = "branding"
    private const val MAX_IMPORT_DIM_PX = 1024

    fun importFromUri(context: Context, slot: BrandingSlot, uri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

            var sampleSize = 1
            while (boundsOpts.outWidth / sampleSize > MAX_IMPORT_DIM_PX ||
                boundsOpts.outHeight / sampleSize > MAX_IMPORT_DIM_PX
            ) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return null

            saveScaledBitmap(context, slot, decoded)
        } catch (_: Exception) {
            null
        }
    }

    fun importFromBytes(context: Context, slot: BrandingSlot, bytes: ByteArray): String? {
        if (bytes.isEmpty()) return null
        return try {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOpts)
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

            var sampleSize = 1
            while (boundsOpts.outWidth / sampleSize > MAX_IMPORT_DIM_PX ||
                boundsOpts.outHeight / sampleSize > MAX_IMPORT_DIM_PX
            ) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
                ?: return null

            saveScaledBitmap(context, slot, decoded)
        } catch (_: Exception) {
            null
        }
    }

    private fun saveScaledBitmap(context: Context, slot: BrandingSlot, decoded: Bitmap): String {
        val scaled = scaleDownIfNeeded(decoded, MAX_IMPORT_DIM_PX)
        if (scaled !== decoded) {
            decoded.recycle()
        }
        val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
        val uniqueSuffix = "${System.currentTimeMillis()}_${System.nanoTime() and 0xFFFF}"
        val target = File(dir, "${slot.key}_$uniqueSuffix.png")
        FileOutputStream(target).use { out ->
            scaled.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        scaled.recycle()
        return target.absolutePath
    }

    fun loadScaledBitmapFromPath(path: String, maxDimPx: Int): Bitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        if (!file.exists() || !file.isFile) return null
        return try {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, boundsOpts)
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null
            var sampleSize = 1
            while (boundsOpts.outWidth / sampleSize > maxDimPx ||
                boundsOpts.outHeight / sampleSize > maxDimPx
            ) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeFile(path, decodeOpts)
        } catch (_: Exception) {
            null
        }
    }

    fun deleteFileIfExists(path: String) {
        if (path.isBlank()) return
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
    }

    fun clearAllCustomImages(context: Context) {
        try {
            val dir = File(context.filesDir, DIR_NAME)
            if (dir.exists()) {
                dir.listFiles()?.forEach { it.delete() }
            }
        } catch (_: Exception) {
        }
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDimPx: Int): Bitmap {
        val largest = max(bitmap.width, bitmap.height)
        if (largest <= maxDimPx) return bitmap
        val ratio = maxDimPx.toFloat() / largest.toFloat()
        val targetW = max(1, (bitmap.width * ratio).roundToInt())
        val targetH = max(1, (bitmap.height * ratio).roundToInt())
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }
}
