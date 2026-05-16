package com.example.comalapp.data.repository

import com.example.comalapp.data.model.User
import com.example.comalapp.data.source.FirebaseAuthSource
import com.example.comalapp.data.source.FirestoreSource
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val authSource: FirebaseAuthSource,
    private val firestoreSource: FirestoreSource,
) {

    suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        authSource.register(email, password)
    }

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        val firebaseUser = authSource.login(email, password)
        val token = FirebaseMessaging.getInstance().token.await()

        firestoreSource.usersCollection
            .document(firebaseUser.uid)
            .update("fcmToken", token)
            .await()

        val snapshot = firestoreSource.usersCollection
            .document(firebaseUser.uid)
            .get()
            .await()

        snapshot.toObject(User::class.java)!!.copy(uid = firebaseUser.uid)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        authSource.sendPasswordResetEmail(email)
    }

    fun logout() {
        authSource.logout()
    }

    fun currentUserId(): String? = authSource.currentUser()?.uid

    fun observeAuthState(): Flow<Boolean> = callbackFlow {
        authSource.observeAuthState { user ->
            trySend(user != null)
        }
        awaitClose()
    }
}