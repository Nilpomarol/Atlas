package com.atlas.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.atlas.data.local.dao.StopPhotoDao
import com.atlas.data.local.dao.TripDao
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
    private val tripDao: TripDao,
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
        tripDao.clearCoverPhotoByFilename(photo.filename)
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
        rows.forEach { row ->
            File(photosDir, row.filename).delete()
            tripDao.clearCoverPhotoByFilename(row.filename)
        }
        dao.deleteAllForStop(stopId, stopType.name)
    }

    override suspend fun countByStop(stopId: String, stopType: StopType): Int =
        withContext(Dispatchers.IO) { dao.countByStop(stopId, stopType.name) }

    private fun compressAndSave(uri: Uri, outputFile: File): Boolean {
        return try {
            // Read EXIF orientation from a dedicated stream before decoding
            val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val raw = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (raw == null) return false

            val oriented = applyExifOrientation(raw, orientation)

            val maxSize = 1920
            val scaled = if (oriented.width > maxSize || oriented.height > maxSize) {
                val ratio = minOf(maxSize.toFloat() / oriented.width, maxSize.toFloat() / oriented.height)
                val w = (oriented.width * ratio).toInt().coerceAtLeast(1)
                val h = (oriented.height * ratio).toInt().coerceAtLeast(1)
                val s = Bitmap.createScaledBitmap(oriented, w, h, true)
                oriented.recycle()
                s
            } else {
                oriented
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

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(-90f); matrix.postScale(-1f, 1f) }
            else -> return bitmap
        }
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (result !== bitmap) bitmap.recycle()
        return result
    }
}
