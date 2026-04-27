package com.example.comalapp.data.repository

import com.example.comalapp.data.model.Order
import com.example.comalapp.data.model.OrderItem
import com.example.comalapp.data.source.FirestoreSource
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class OrderRepository(
    private val firestoreSource: FirestoreSource
) {

    private val allowedTransitions = mapOf(
        "pending" to setOf("preparing", "cancelled"),
        "preparing" to setOf("ready"),
        "ready" to setOf("delivered")
    )

    suspend fun createOrder(
        userId: String,
        items: List<Pair<com.example.comalapp.data.model.Product, Int>>,
    ): Result<String> = runCatching {
        val unavailable = items.filter { !it.first.available }
        if (unavailable.isNotEmpty()) error("Productos no disponibles: ${unavailable.map { it.first.name }}")

        val orderRef = firestoreSource.ordersCollection.document()
        val qrCode = UUID.randomUUID().toString()
        val orderItems = items.map { (product, quantity) ->
            val itemRef = firestoreSource.orderItemsCollection.document()
            Triple(itemRef, product, quantity)
        }

        val total = orderItems.sumOf { (_, product, quantity) -> product.price * quantity }
        val productCount = orderItems.sumOf { it.third }

        val order = Order(
            id = orderRef.id,
            userId = userId,
            status = "pending",
            total = total,
            productCount = productCount,
            qrCode = qrCode,
            createdAt = Timestamp.now()
        )

        val batch = firestoreSource.db.batch()
        batch.set(orderRef, order)

        orderItems.forEach { (itemRef, product, quantity) ->
            val orderItem = OrderItem(
                id = itemRef.id,
                orderId = orderRef.id,
                productId = product.id,
                quantity = quantity,
                subtotal = product.price * quantity
            )
            batch.set(itemRef, orderItem)
        }

        batch.commit().await()
        orderRef.id
    }

    fun observeOrder(orderId: String): Flow<Result<Order>> = callbackFlow {
        val listener = firestoreSource.ordersCollection
            .document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(Order::class.java)?.copy(id = snapshot.id)
                if (order != null) trySend(Result.success(order))
            }
        awaitClose { listener.remove() }
    }

    fun observeActiveOrders(): Flow<Result<List<Order>>> = callbackFlow {
        val listener = firestoreSource.ordersCollection
            .whereNotIn("status", listOf("delivered", "cancelled"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(orders))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserOrderHistory(userId: String): Result<List<Order>> = runCatching {
        firestoreSource.ordersCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun getAllOrders(): Result<List<Order>> = runCatching {
        firestoreSource.ordersCollection
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        currentStatus: String,
        newStatus: String,
        requestedByRole: String,
    ): Result<Unit> = runCatching {
        if (currentStatus == "delivered") error("Una orden entregada es inmutable")
        if (newStatus == "delivered" && requestedByRole != "worker") error("Solo un worker puede marcar como entregado")

        val allowed = allowedTransitions[currentStatus] ?: emptySet()
        if (newStatus !in allowed) error("Transición inválida: $currentStatus -> $newStatus")

        firestoreSource.ordersCollection
            .document(orderId)
            .update("status", newStatus)
            .await()
    }

    suspend fun cancelOrder(orderId: String, currentStatus: String): Result<Unit> = runCatching {
        if (currentStatus != "pending") error("Solo se puede cancelar una orden en estado pending")
        firestoreSource.ordersCollection
            .document(orderId)
            .update("status", "cancelled")
            .await()
    }

    suspend fun getOrderItems(orderId: String): Result<List<OrderItem>> = runCatching {
        firestoreSource.orderItemsCollection
            .whereEqualTo("orderId", orderId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(OrderItem::class.java)?.copy(id = doc.id)
            }
    }
}