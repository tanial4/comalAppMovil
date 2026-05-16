package com.example.comalapp.data.source

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthSource {

    private val auth = FirebaseAuth.getInstance()

    suspend fun register(email: String, password: String): FirebaseUser =
        auth.createUserWithEmailAndPassword(email, password).await().user!!

    suspend fun login(email: String, password: String): FirebaseUser =
        auth.signInWithEmailAndPassword(email, password).await().user!!

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    fun observeAuthState(onChanged: (FirebaseUser?) -> Unit) {
        auth.addAuthStateListener { onChanged(it.currentUser) }
    }
}