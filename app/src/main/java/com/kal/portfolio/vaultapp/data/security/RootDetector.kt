package com.kal.portfolio.vaultapp.data.security

import android.content.pm.PackageManager
import android.content.Context
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootDetector @Inject constructor(
    private val context: Context
) {

    fun isRooted(): Boolean {
        return checkSuBinary()
                || checkDangerousPackages()
                || checkBuildTags()
                || checkWritableSystem()
    }

    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/su",
        )
        return paths.any { File(it).exists() }
    }

    private fun checkDangerousPackages(): Boolean {
        val packages = listOf(
            "com.topjohnwu.magisk",
            "com.noshufou.android.su",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
        )
        return packages.any {
            try {
                context.packageManager.getPackageInfo(it, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private fun checkBuildTags(): Boolean {
        return android.os.Build.TAGS?.contains("test-keys") == true
    }

    private fun checkWritableSystem(): Boolean {
        return try {
            val file = File("/system/test_write_${System.currentTimeMillis()}")
            file.createNewFile().also { file.delete() }
        } catch (e: Exception) {
            false
        }
    }
}