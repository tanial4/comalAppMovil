package com.example.comalapp.data

import com.example.comalapp.data.repository.AuthRepository
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
}