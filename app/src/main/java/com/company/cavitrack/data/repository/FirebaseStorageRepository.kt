package com.company.cavitrack.data.repository

import android.net.Uri
import com.company.cavitrack.domain.repository.StorageRepository
import com.company.cavitrack.util.DataResult
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadPhoto(file: File, path: String): DataResult<String> {
        return try {
            val fileRef = storage.reference.child(path)
            fileRef.putFile(Uri.fromFile(file)).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            DataResult.Success(downloadUrl)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to upload photo")
        }
    }

    override suspend fun deletePhoto(urlOrPath: String): DataResult<Unit> {
        return try {
            val ref = if (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://")) {
                storage.getReferenceFromUrl(urlOrPath)
            } else {
                storage.reference.child(urlOrPath)
            }
            ref.delete().await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to delete photo")
        }
    }

    override suspend fun deleteUserPhotos(userId: String): DataResult<Unit> {
        return try {
            val userFolderRef = storage.reference.child("photos/$userId")
            val listResult = userFolderRef.listAll().await()
            listResult.items.forEach { item ->
                item.delete().await()
            }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to delete user photos")
        }
    }
}
