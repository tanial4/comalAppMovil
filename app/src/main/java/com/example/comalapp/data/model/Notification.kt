package com.example.comalapp.data.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val userId: String = "",
    val orderId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val createdAt: Timestamp? = null,
    val read: Boolean = false
)