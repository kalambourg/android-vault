package com.kal.portfolio.vaultapp.data.security

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntegrityChecker @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val EXPECTED_SIGNATURE_HASH = "BUILD_TIME_HASH_PLACEHOLDER"
    }

    fun isSignatureValid(): Boolean {
        if (isDebugBuild()) return true

        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signingInfo = packageInfo.signingInfo
            val signatures = signingInfo?.apkContentsSigners ?: return false

            val md = MessageDigest.getInstance("SHA-256")
            val currentHash = Base64.encodeToString(
                md.digest(signatures[0].toByteArray()),
                Base64.DEFAULT
            ).trim()

            currentHash == EXPECTED_SIGNATURE_HASH
        } catch (_: Exception) {
            false
        }
    }

    private fun isDebugBuild(): Boolean {
        return (context.applicationInfo.flags and
                android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}