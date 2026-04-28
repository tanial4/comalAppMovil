package com.example.comalapp.data.repository

import com.example.comalapp.data.model.User
import com.example.comalapp.data.source.FirestoreSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestoreSource: FirestoreSource
) {

    suspend fun createUser(user: User): Result<Unit> = runCatching {
        firestoreSource.usersCollection
            .document(user.uid)
            .set(user)
            .await()
    }

    suspend fun getUserById(uid: String): Result<User> = runCatching {
        val snapshot = firestoreSource.usersCollection
            .document(uid)
            .get()
            .await()

        snapshot.toObject(User::class.java)!!.copy(uid = uid)
    }

    suspend fun getAllUsers(): Result<List<User>> = runCatching {
        firestoreSource.usersCollection
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(uid = doc.id)
            }
    }

    suspend fun updateUserRole(uid: String, role: String): Result<Unit> = runCatching {
        firestoreSource.usersCollection
            .document(uid)
            .update("role", role)
            .await()
    }

    suspend fun deleteUser(uid: String): Result<Unit> = runCatching {
        firestoreSource.usersCollection
            .document(uid)
            .delete()
            .await()
    }
    fun observeAllUsers(): Flow<Result<List<User>>> = callbackFlow {
        val listener = firestoreSource.usersCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(uid = doc.id)
                } ?: emptyList()
                trySend(Result.success(users))
            }
        awaitClose { listener.remove() }
    }
}