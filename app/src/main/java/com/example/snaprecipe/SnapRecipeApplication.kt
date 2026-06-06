package com.example.snaprecipe

import android.app.Application
import com.example.snaprecipe.di.AppContainer

/** Application subclass that owns the manual DI container. */
class SnapRecipeApplication : Application() {

    // Created once for the process lifetime; accessed by the ViewModel factory.
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
