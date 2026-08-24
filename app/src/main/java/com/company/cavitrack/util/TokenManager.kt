package com.company.cavitrack.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {
    fun hasValidToken(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun clearToken() {
        firebaseAuth.signOut()
    }
}
