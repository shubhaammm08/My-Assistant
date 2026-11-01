package com.jarvis.assistant

import android.app.Application
import android.util.Log
import com.jarvis.assistant.data.local.JarvisDatabase
import com.jarvis.assistant.offline.SyncWorker

class JarvisApplication : Application() {
    val database by lazy { JarvisDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        Log.i("JarvisApp", "JARVIS Application Initialized.")
        
        // Schedule periodic task synchronization
        SyncWorker.schedulePeriodicSync(this)
    }
}

