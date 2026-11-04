package com.example.financeapp

import android.app.Application
import com.example.financeapp.data.ApiClient
import com.example.financeapp.data.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val saved = runBlocking { Preferences(this@App).serverUrl.first() }
        if (saved.isNotBlank()) ApiClient.setBaseUrl(saved)
    }
}
