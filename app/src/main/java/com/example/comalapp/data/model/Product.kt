package com.example.comalapp.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val available: Boolean = true,
    val imageUrl: String = "",
    val categoryId: String = ""
)