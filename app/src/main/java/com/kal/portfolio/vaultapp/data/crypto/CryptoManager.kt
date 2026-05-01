package com.kal.portfolio.vaultapp.data.crypto

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor(
    private val keystoreManager: KeystoreManager
) {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_SIZE = 128
    }

    fun getEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keystoreManager.getOrCreateKey())
        return cipher
    }

    fun getDecryptCipher(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            keystoreManager.getOrCreateKey(),
            GCMParameterSpec(TAG_SIZE, iv)
        )
        return cipher
    }

    fun encrypt(cipher: Cipher, data: String): EncryptedData {
        val ciphertext = cipher.doFinal(data.toByteArray())
        return EncryptedData(iv = cipher.iv, ciphertext = ciphertext)
    }

    fun decrypt(cipher: Cipher, encryptedData: EncryptedData): String {
        return String(cipher.doFinal(encryptedData.ciphertext))
    }
}

data class EncryptedData(
    val iv: ByteArray,
    val ciphertext: ByteArray
) {
    fun toBase64(): String {
        val combined = iv + ciphertext
        return android.util.Base64.encodeToString(combined, android.util.Base64.DEFAULT)
    }

    companion object {
        fun fromBase64(base64: String): EncryptedData {
            val combined = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val iv = combined.sliceArray(0 until 12)
            val ciphertext = combined.sliceArray(12 until combined.size)
            return EncryptedData(iv = iv, ciphertext = ciphertext)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedData) return false
        return iv.contentEquals(other.iv) && ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + ciphertext.contentHashCode()
        return result
    }
}