package com.example.financeapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "finance_settings")

class Preferences(private val context: Context) {
    private val darkKey = booleanPreferencesKey("dark_theme")
    private val userKey = intPreferencesKey("user_id")
    private val serverKey = stringPreferencesKey("server_url")
    val darkTheme = context.dataStore.data.map { it[darkKey] ?: false }
    val userId = context.dataStore.data.map { it[userKey] ?: 0 }
    val serverUrl = context.dataStore.data.map { it[serverKey] ?: "" }
    suspend fun setDarkTheme(value: Boolean) { context.dataStore.edit { it[darkKey] = value } }
    suspend fun setUserId(value: Int) { context.dataStore.edit { it[userKey] = value } }
    suspend fun setServerUrl(value: String) { context.dataStore.edit { it[serverKey] = value } }
    suspend fun clearUser() { context.dataStore.edit { it.remove(userKey) } }
}
