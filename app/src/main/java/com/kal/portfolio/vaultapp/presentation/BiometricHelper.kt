package com.kal.portfolio.vaultapp.presentation

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

class BiometricHelper(
    private val activity: FragmentActivity,
    private val onSuccess: (Cipher) -> Unit,
    private val onError: (String) -> Unit
) {

    fun authenticate(cipher: Cipher) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                val unlockedCipher = result.cryptoObject?.cipher
                if (unlockedCipher != null) {
                    onSuccess(unlockedCipher)
                } else {
                    onError("Cipher non disponible après authentification")
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onError("Authentification échouée")
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Vault")
            .setSubtitle("Authentifiez-vous pour accéder à vos secrets")
            .setNegativeButtonText("Annuler")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        BiometricPrompt(activity, executor, callback)
            .authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}