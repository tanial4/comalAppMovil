package com.example.comalapp.data.repository

import android.net.Uri
import com.example.comalapp.data.model.Product
import com.example.comalapp.data.source.FirestoreSource
import com.example.comalapp.data.source.StorageSource
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val firestoreSource: FirestoreSource,
    private val storageSource: StorageSource
) {

    suspend fun getAvailableProducts(): Result<List<Product>> = runCatching {
        firestoreSource.productsCollection
            .whereEqualTo("available", true)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun getAllProducts(): Result<List<Product>> = runCatching {
        firestoreSource.productsCollection
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun createProduct(product: Product, imageUri: Uri): Result<Unit> = runCatching {
        val docRef = firestoreSource.productsCollection.document()
        val imageUrl = storageSource.uploadProductImage(docRef.id, imageUri)
        docRef.set(product.copy(id = docRef.id, imageUrl = imageUrl)).await()
    }

    suspend fun updateProduct(product: Product, imageUri: Uri?): Result<Unit> = runCatching {
        val imageUrl = if (imageUri != null) {
            storageSource.uploadProductImage(product.id, imageUri)
        } else {
            product.imageUrl
        }
        firestoreSource.productsCollection
            .document(product.id)
            .set(product.copy(imageUrl = imageUrl))
            .await()
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        storageSource.deleteProductImage(productId)
        firestoreSource.productsCollection
            .document(productId)
            .delete()
            .await()
    }

    suspend fun setProductAvailability(productId: String, available: Boolean): Result<Unit> = runCatching {
        firestoreSource.productsCollection
            .document(productId)
            .update("available", available)
            .await()
    }
}