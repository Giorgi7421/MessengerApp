package ge.gpavliashvili.messenger.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val storage = FirebaseStorage.getInstance().reference

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            val imageRef = storage.child("profile_images/$userId.jpg")
            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfileImage(userId: String): Result<Unit> {
        return try {
            val imageRef = storage.child("profile_images/$userId.jpg")
            imageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 