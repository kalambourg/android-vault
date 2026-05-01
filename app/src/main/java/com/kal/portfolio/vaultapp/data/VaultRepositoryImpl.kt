package com.kal.portfolio.vaultapp.data

import com.kal.portfolio.vaultapp.data.crypto.CryptoManager
import com.kal.portfolio.vaultapp.data.crypto.EncryptedData
import com.kal.portfolio.vaultapp.data.storage.EncryptedStorage
import com.kal.portfolio.vaultapp.domain.VaultRepository
import com.kal.portfolio.vaultapp.domain.model.VaultEntry
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepositoryImpl @Inject constructor(
    private val cryptoManager: CryptoManager,
    private val storage: EncryptedStorage
) : VaultRepository {

    override fun getAll(): List<VaultEntry> {
        return storage.loadEntries()
    }

    override fun save(label: String, value: String, encryptCipher: Cipher) {
        val encryptedData = cryptoManager.encrypt(encryptCipher, value)
        val entry = VaultEntry(
            label = label,
            encryptedValue = encryptedData.toBase64()
        )
        val current = storage.loadEntries().toMutableList()
        current.add(entry)
        storage.saveEntries(current)
    }

    override fun decrypt(entry: VaultEntry, decryptCipher: Cipher): String {
        val encryptedData = EncryptedData.fromBase64(entry.encryptedValue)
        return cryptoManager.decrypt(decryptCipher, encryptedData)
    }

    override fun delete(id: String) {
        val current = storage.loadEntries().toMutableList()
        current.removeAll { it.id == id }
        storage.saveEntries(current)
    }
}