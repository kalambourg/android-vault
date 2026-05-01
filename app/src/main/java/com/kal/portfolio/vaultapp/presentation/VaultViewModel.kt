package com.kal.portfolio.vaultapp.presentation

import androidx.lifecycle.ViewModel
import com.kal.portfolio.vaultapp.data.crypto.CryptoManager
import com.kal.portfolio.vaultapp.data.security.EmulatorDetector
import com.kal.portfolio.vaultapp.data.security.IntegrityChecker
import com.kal.portfolio.vaultapp.data.security.RootDetector
import com.kal.portfolio.vaultapp.domain.VaultRepository
import com.kal.portfolio.vaultapp.domain.model.VaultEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.crypto.Cipher
import javax.inject.Inject

data class VaultUiState(
    val entries: List<VaultEntry> = emptyList(),
    val isLocked: Boolean = true,
    val securityError: String? = null,
    val decryptedValues: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: VaultRepository,
    private val cryptoManager: CryptoManager,
    private val rootDetector: RootDetector,
    private val emulatorDetector: EmulatorDetector,
    private val integrityChecker: IntegrityChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        runSecurityChecks()
    }

    private fun runSecurityChecks() {
        when {
            rootDetector.isRooted() -> setSecurityError("Device rooté détecté")
            emulatorDetector.isEmulator() -> setSecurityError("Émulateur détecté")
            !integrityChecker.isSignatureValid() -> setSecurityError("Intégrité de l'app compromise")
            else -> loadEntries()
        }
    }

    private fun setSecurityError(message: String) {
        _uiState.update { it.copy(securityError = message) }
    }

    private fun loadEntries() {
        _uiState.update { it.copy(entries = repository.getAll()) }
    }

    fun getEncryptCipher(): Cipher {
        return cryptoManager.getEncryptCipher()
    }

    fun getDecryptCipher(iv: ByteArray): Cipher {
        return cryptoManager.getDecryptCipher(iv)
    }

    fun saveEntry(label: String, value: String, cipher: Cipher) {
        try {
            repository.save(label, value, cipher)
            loadEntries()
            _uiState.update { it.copy(error = null) }
        } catch (_: Exception) {
            _uiState.update { it.copy(error = "Erreur lors de la sauvegarde") }
        }
    }

    fun decryptEntry(entry: VaultEntry, cipher: Cipher) {
        try {
            val decrypted = repository.decrypt(entry, cipher)
            _uiState.update {
                it.copy(decryptedValues = it.decryptedValues + (entry.id to decrypted))
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(error = "Erreur lors du déchiffrement") }
        }
    }

    fun deleteEntry(id: String) {
        repository.delete(id)
        _uiState.update {
            it.copy(
                entries = repository.getAll(),
                decryptedValues = it.decryptedValues - id
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun lockVault() {
        _uiState.update {
            it.copy(
                isLocked = true,
                decryptedValues = emptyMap()
            )
        }
    }
}