package com.university.abiturient.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoStorage {

    private const val PHOTOS_DIR = "photos"

    fun photosDirectory(context: Context): File {
        val dir = File(context.filesDir, PHOTOS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createImageFile(context: Context): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(photosDirectory(context), "NOTE_$stamp.jpg")
    }

    fun uriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun deleteIfExists(path: String?) {
        if (path.isNullOrBlank()) return
        File(path).takeIf { it.exists() }?.delete()
    }

    fun copyFromGalleryUri(context: Context, uri: Uri): String? {
        val dest = createImageFile(context)
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            input.use { stream ->
                dest.outputStream().use { out -> stream.copyTo(out) }
            }
            dest.absolutePath
        } catch (_: Exception) {
            deleteIfExists(dest.absolutePath)
            null
        }
    }

    fun grantCameraUriPermissions(context: Context, intent: Intent, uri: Uri) {
        val resolvers = context.packageManager.queryIntentActivities(
            intent,
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolve in resolvers) {
            context.grantUriPermission(
                resolve.activityInfo.packageName,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }
}
