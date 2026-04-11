package com.example.comalapp.data.repository

import com.example.comalapp.data.model.Notification
import com.example.comalapp.data.source.FirestoreSource
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestoreSource: FirestoreSource
) {

    suspend fun createNotification(
        userId: String,
        orderId: String,
        title: String,
        message: String,
        type: String
    ): Result<Unit> = runCatching {
        val docRef = firestoreSource.notificationsCollection.document()
        val notification = Notification(
            id = docRef.id,
            userId = userId,
            orderId = orderId,
            title = title,
            message = message,
            type = type,
            createdAt = Timestamp.now(),
            read = false
        )
        docRef.set(notification).await()
    }

    fun observeUserNotifications(userId: String): Flow<Result<List<Notification>>> = callbackFlow {
        val listener = firestoreSource.notificationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(notifications))
            }
        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> = runCatching {
        firestoreSource.notificationsCollection
            .document(notificationId)
            .update("read", true)
            .await()
    }
}