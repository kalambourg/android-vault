package com.kal.portfolio.vaultapp.domain

import com.kal.portfolio.vaultapp.domain.model.VaultEntry
import javax.crypto.Cipher

interface VaultRepository {
    fun getAll(): List<VaultEntry>
    fun save(label: String, value: String, encryptCipher: Cipher)
    fun decrypt(entry: VaultEntry, decryptCipher: Cipher): String
    fun delete(id: String)
}