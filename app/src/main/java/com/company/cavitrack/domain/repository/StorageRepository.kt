package com.company.cavitrack.domain.repository

import com.company.cavitrack.util.DataResult
import java.io.File

interface StorageRepository {
    suspend fun uploadPhoto(file: File, path: String): DataResult<String>
    suspend fun deletePhoto(urlOrPath: String): DataResult<Unit>
    suspend fun deleteUserPhotos(userId: String): DataResult<Unit>
}
