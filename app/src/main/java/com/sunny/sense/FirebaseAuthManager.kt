package com.sunny.sense

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthManager(private val context: Context) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("123456789012-mockclientid.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signOut() {
        auth.signOut()
        getGoogleSignInClient().signOut()
    }
}
