package com.kal.portfolio.vaultapp.data.security

import android.os.Build
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmulatorDetector @Inject constructor() {

    fun isEmulator(): Boolean {
        return checkBuildProps()
                || checkHardware()
                || checkFiles()
    }

    private fun checkBuildProps(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic"))
    }

    private fun checkHardware(): Boolean {
        return (Build.HARDWARE == "goldfish"
                || Build.HARDWARE == "ranchu"
                || Build.PRODUCT == "sdk_gphone64_x86_64")
    }

    private fun checkFiles(): Boolean {
        val emulatorFiles = arrayOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
        )
        return emulatorFiles.any { File(it).exists() }
    }
}