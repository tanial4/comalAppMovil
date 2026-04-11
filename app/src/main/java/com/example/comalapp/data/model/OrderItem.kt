package com.example.comalapp.data.model

data class OrderItem(
    val id: String = "",
    val orderId: String = "",
    val productId: String = "",
    val quantity: Int = 0,
    val subtotal: Double = 0.0
)