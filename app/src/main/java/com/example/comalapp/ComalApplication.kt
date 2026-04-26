package com.example.comalapp

import android.app.Application
import com.example.comalapp.data.AppContainer

class ComalApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}