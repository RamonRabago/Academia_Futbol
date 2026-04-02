package com.escuelafutbol.academia.data.sync

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.io.File

internal const val ACADEMIA_MEDIA_BUCKET = "academia-media"

internal suspend fun SupabaseClient.uploadAcademiaPublicImage(objectPath: String, file: File): String {
    val bytes = file.readBytes()
    val ext = file.extension.lowercase().let { if (it == "jpeg") "jpg" else it }
    val contentType = when (ext) {
        "png" -> ContentType.Image.PNG
        "webp" -> ContentType.parse("image/webp")
        else -> ContentType.Image.JPEG
    }
    storage.from(ACADEMIA_MEDIA_BUCKET).upload(objectPath, bytes) {
        upsert = true
        this.contentType = contentType
    }
    return storage.from(ACADEMIA_MEDIA_BUCKET).publicUrl(objectPath)
}

/** PDF o imagen (acta, etc.). */
internal suspend fun SupabaseClient.uploadAcademiaPublicBinary(objectPath: String, file: File): String {
    val bytes = file.readBytes()
    val ext = file.extension.lowercase().let { if (it == "jpeg") "jpg" else it }
    val contentType = when (ext) {
        "pdf" -> ContentType.Application.Pdf
        "png" -> ContentType.Image.PNG
        "webp" -> ContentType.parse("image/webp")
        else -> ContentType.Image.JPEG
    }
    storage.from(ACADEMIA_MEDIA_BUCKET).upload(objectPath, bytes) {
        upsert = true
        this.contentType = contentType
    }
    return storage.from(ACADEMIA_MEDIA_BUCKET).publicUrl(objectPath)
}
