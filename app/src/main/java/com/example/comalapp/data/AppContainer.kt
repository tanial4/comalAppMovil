package com.example.comalapp.data

import com.example.comalapp.data.repository.AuthRepository
import com.example.comalapp.data.repository.CategoryRepository
import com.example.comalapp.data.repository.NotificationRepository
import com.example.comalapp.data.repository.OrderRepository
import com.example.comalapp.data.repository.ProductRepository
import com.example.comalapp.data.repository.UserRepository
import com.example.comalapp.data.source.FirebaseAuthSource
import com.example.comalapp.data.source.FirestoreSource
import com.example.comalapp.data.source.StorageSource

class AppContainer {
    private val authSource = FirebaseAuthSource()
    private val firestoreSource = FirestoreSource()
    private val storageSource = StorageSource()

    val authRepository = AuthRepository(authSource, firestoreSource)
    val userRepository = UserRepository(firestoreSource)
    val categoryRepository = CategoryRepository(firestoreSource)
    val productRepository = ProductRepository(firestoreSource, storageSource)
    val orderRepository = OrderRepository(firestoreSource)
    val notificationRepository = NotificationRepository(firestoreSource)
}