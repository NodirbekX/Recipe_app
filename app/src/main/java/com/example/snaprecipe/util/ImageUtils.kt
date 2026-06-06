package com.example.snaprecipe.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

/** Bitmap helpers for preparing a photo for the vision request. */
object ImageUtils {

    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 85

    /**
     * Decodes a content:// (camera capture or gallery pick) [uri] into a Bitmap.
     *
     * Forces a software allocator on API 28+: `ImageDecoder` defaults to a hardware
     * bitmap, which cannot be read back / JPEG-compressed and would crash the encode
     * step. Returns null on any failure so the caller can degrade gracefully.
     */
    fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = false
                }
            } else {
                @Suppress("DEPRECATION")
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Scales [bitmap] down so its longest edge is at most [maxDimension], preserving
     * aspect ratio. Returns the original if it already fits (smaller payload + faster
     * upload, and well within the model's supported image size).
     */
    fun resize(bitmap: Bitmap, maxDimension: Int = MAX_DIMENSION): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).roundToInt().coerceAtLeast(1)
        val newHeight = (height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /** Encodes [bitmap] as a base64 JPEG string (no line wrapping) for the API body. */
    fun toBase64Jpeg(bitmap: Bitmap, quality: Int = JPEG_QUALITY): String {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    /** Convenience: resize then encode in one call. */
    fun resizeAndEncode(bitmap: Bitmap): String = toBase64Jpeg(resize(bitmap))
}
