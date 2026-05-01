package com.kal.portfolio.vaultapp.domain.model

import java.util.UUID

data class VaultEntry(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val encryptedValue: String, // Base64(iv + ciphertext)
    val createdAt: Long = System.currentTimeMillis()
)