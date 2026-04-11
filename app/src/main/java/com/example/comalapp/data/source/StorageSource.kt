package com.example.comalapp.data.source

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageSource {

    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadProductImage(productId: String, imageUri: Uri): String {
        val ref = storage.reference.child("products/$productId.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteProductImage(productId: String) {
        runCatching {
            storage.reference.child("products/$productId.jpg").delete().await()
        }
    }
}