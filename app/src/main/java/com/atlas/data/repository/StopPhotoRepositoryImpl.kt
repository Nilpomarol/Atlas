package com.atlas.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.atlas.data.local.dao.StopPhotoDao
import com.atlas.data.local.entity.StopPhotoEntity
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.repository.StopPhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.util.UUID

class StopPhotoRepositoryImpl(
    private val context: Context,
    private val dao: StopPhotoDao,
) : StopPhotoRepository {

    override fun observeByStop(stopId: String, stopType: StopType): Flow<List<StopPhoto>> =
        dao.observeByStop(stopId, stopType.name).map { it.map { e -> e.toDomain() } }

    override fun observeByStopIds(stopIds: List<String>, stopType: StopType): Flow<List<StopPhoto>> {
        if (stopIds.isEmpty()) return flowOf(emptyList())
        return dao.observeByStopIds(stopIds, stopType.name).map { it.map { e -> e.toDomain() } }
    }

    override suspend fun addPhotos(stopId: String, stopType: StopType, uris: List<Uri>): List<StopPhoto> =
        withContext(Dispatchers.IO) {
            val now = Instant.now().toString()
            val nextOrder = (dao.getMaxSortOrder(stopId, stopType.name) ?: -1) + 1
            val photosDir = File(context.filesDir, "photos").apply { mkdirs() }

            uris.mapIndexedNotNull { i, uri ->
                val filename = "${UUID.randomUUID()}.jpg"
                val success = compressAndSave(uri, File(photosDir, filename))
                if (!success) return@mapIndexedNotNull null

                val entity = StopPhotoEntity(
                    id = UUID.randomUUID().toString(),
                    stopId = stopId,
                    stopType = stopType.name,
                    filename = filename,
                    sortOrder = nextOrder + i,
                    createdAt = now,
                )
                dao.insert(entity)
                entity.toDomain()
            }
        }

    override suspend fun deletePhoto(photo: StopPhoto) = withContext(Dispatchers.IO) {
        File(context.filesDir, "photos/${photo.filename}").delete()
        dao.delete(
            StopPhotoEntity(
                id = photo.id,
                stopId = photo.stopId,
                stopType = photo.stopType.name,
                filename = photo.filename,
                sortOrder = photo.sortOrder,
                createdAt = photo.createdAt,
            ),
        )
    }

    override suspend fun deleteAllForStop(stopId: String, stopType: StopType) = withContext(Dispatchers.IO) {
        val rows = dao.getByStop(stopId, stopType.name)
        val photosDir = File(context.filesDir, "photos")
        rows.forEach { File(photosDir, it.filename).delete() }
        dao.deleteAllForStop(stopId, stopType.name)
    }

    override suspend fun countByStop(stopId: String, stopType: StopType): Int =
        withContext(Dispatchers.IO) { dao.countByStop(stopId, stopType.name) }

    private fun compressAndSave(uri: Uri, outputFile: File): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return false

            val maxSize = 1920
            val scaled = if (bitmap.width > maxSize || bitmap.height > maxSize) {
                val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
                val w = (bitmap.width * ratio).toInt().coerceAtLeast(1)
                val h = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                val s = Bitmap.createScaledBitmap(bitmap, w, h, true)
                bitmap.recycle()
                s
            } else {
                bitmap
            }

            FileOutputStream(outputFile).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            scaled.recycle()
            true
        } catch (_: Exception) {
            false
        }
    }
}
