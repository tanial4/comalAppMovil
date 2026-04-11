package com.example.comalapp.data.source

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreSource {
    val db = FirebaseFirestore.getInstance()

    val usersCollection get() = db.collection("users")
    val categoriesCollection get() = db.collection("categories")
    val productsCollection get() = db.collection("products")
    val ordersCollection get() = db.collection("orders")
    val orderItemsCollection get() = db.collection("orderItems")
    val notificationsCollection get() = db.collection("notifications")
}