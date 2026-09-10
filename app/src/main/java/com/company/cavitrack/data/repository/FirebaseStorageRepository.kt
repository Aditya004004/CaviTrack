package com.company.cavitrack.data.repository

import android.net.Uri
import com.company.cavitrack.domain.repository.StorageRepository
import com.company.cavitrack.util.DataResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()
            fileRef.putFile(Uri.fromFile(file), metadata).await()
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
            suspend fun deleteRecursive(ref: com.google.firebase.storage.StorageReference) {
                val listResult = ref.listAll().await()
                coroutineScope {
                    listResult.items.chunked(10).forEach { chunk ->
                        chunk.map { item -> async { item.delete().await() } }.awaitAll()
                    }
                }
                // Recurse into subdirectories
                for (prefix in listResult.prefixes) {
                    deleteRecursive(prefix)
                }
            }
            val userFolderRef = storage.reference.child("photos/$userId")
            deleteRecursive(userFolderRef)
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to delete user photos")
        }
    }
}
