package com.example.comalapp.data.model

import com.google.firebase.Timestamp

data class Order(
    val id: String = "",
    val userId: String = "",
    val status: String = "pending",
    val total: Double = 0.0,
    val productCount: Int = 0,
    val qrCode: String = "",
    val createdAt: Timestamp? = null
)