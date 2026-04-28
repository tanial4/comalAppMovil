package com.example.comalapp.data.repository

import com.example.comalapp.data.model.Category
import com.example.comalapp.data.source.FirestoreSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepository(
    private val firestoreSource: FirestoreSource
) {

    fun observeCategories(): Flow<Result<List<Category>>> = callbackFlow {
        val listener = firestoreSource.categoriesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(categories))
            }
        awaitClose { listener.remove() }
    }

    suspend fun createCategory(name: String): Result<Unit> = runCatching {
        val docRef = firestoreSource.categoriesCollection.document()
        val category = Category(id = docRef.id, name = name)
        docRef.set(category).await()
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = runCatching {
        firestoreSource.categoriesCollection
            .document(categoryId)
            .delete()
            .await()
    }
}