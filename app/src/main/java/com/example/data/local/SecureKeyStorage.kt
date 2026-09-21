package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureKeyStorage(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("SecureKeyStorage", "Failed to create EncryptedSharedPreferences, fallback to standard", e)
        context.getSharedPreferences("${PREFS_NAME}_fallback", Context.MODE_PRIVATE)
    }

    fun saveGroqApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GROQ_API_KEY, apiKey.trim()).apply()
    }

    fun getGroqApiKey(): String? {
        val key = prefs.getString(KEY_GROQ_API_KEY, null)?.trim()
        return if (key.isNullOrEmpty()) null else key
    }

    fun clearGroqApiKey() {
        prefs.edit().remove(KEY_GROQ_API_KEY).apply()
    }

    fun hasGroqApiKey(): Boolean {
        return !getGroqApiKey().isNullOrEmpty()
    }

    companion object {
        private const val PREFS_NAME = "salim_secure_prefs"
        private const val KEY_GROQ_API_KEY = "groq_api_key"
    }
}
