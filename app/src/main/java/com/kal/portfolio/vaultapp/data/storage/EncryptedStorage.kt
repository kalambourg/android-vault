package com.kal.portfolio.vaultapp.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.kal.portfolio.vaultapp.domain.model.VaultEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedStorage @Inject constructor(
    context: Context
) {
    companion object {
        private const val PREFS_NAME = "vault_store"
        private const val ENTRIES_KEY = "entries"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    fun saveEntries(entries: List<VaultEntry>) {
        prefs.edit()
            .putString(ENTRIES_KEY, gson.toJson(entries))
            .apply()
    }

    fun loadEntries(): List<VaultEntry> {
        val json = prefs.getString(ENTRIES_KEY, null) ?: return emptyList()
        return gson.fromJson(json, Array<VaultEntry>::class.java).toList()
    }
}