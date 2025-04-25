package edu.cit.astroglow

import android.app.Application
import com.google.firebase.FirebaseApp

class AstroGlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
} 