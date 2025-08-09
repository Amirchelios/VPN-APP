package com.example.vpnapp.core

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

object CoreFetcher {
    private fun abiTag(): String {
        val abis = android.os.Build.SUPPORTED_ABIS.map { it.lowercase() }
        return when {
            abis.any { it.contains("arm64") || it.contains("aarch64") } -> "arm64-v8a"
            abis.any { it.contains("armeabi-v7a") || it.contains("armv7") } -> "armeabi-v7a"
            abis.any { it.contains("x86_64") } -> "x86_64"
            abis.any { it.contains("x86") } -> "x86"
            else -> "arm64-v8a"
        }
    }

    private fun candidateUrls(abi: String): List<String> {
        // Known patterns for Xray-core Android community builds. May vary by release; try several.
        val base = "https://github.com/XTLS/Xray-core/releases/latest/download"
        return when (abi) {
            "arm64-v8a" -> listOf(
                "$base/Xray-android-arm64-v8a.zip",
                "$base/Xray-android-arm64.zip"
            )
            "armeabi-v7a" -> listOf(
                "$base/Xray-android-armeabi-v7a.zip",
                "$base/Xray-android-armv7.zip"
            )
            "x86_64" -> listOf(
                "$base/Xray-android-x86_64.zip"
            )
            "x86" -> listOf(
                "$base/Xray-android-x86.zip"
            )
            else -> emptyList()
        }
    }

    private fun log(context: Context, message: String) {
        try {
            val f = File(CoreManagerInternal.coreDir(context), "xray.log")
            f.appendText(message + "\n")
        } catch (_: Exception) {}
    }

    // Internal access to CoreManager paths without exposing them publicly
    private object CoreManagerInternal {
        fun coreDir(context: Context): File = context.getDir("bin", Context.MODE_PRIVATE)
        fun coreBinary(context: Context): File = File(coreDir(context), "xray")
    }

    fun downloadAndInstall(context: Context): Boolean {
        val abi = abiTag()
        val urls = candidateUrls(abi)
        if (urls.isEmpty()) {
            log(context, "[error] No download URL patterns for abi=$abi")
            return false
        }
        for (u in urls) {
            try {
                log(context, "[info] Trying to download core from $u")
                val conn = URL(u).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = true
                conn.connect()
                if (conn.responseCode in 200..299) {
                    ZipInputStream(conn.inputStream).use { zip ->
                        var entry = zip.nextEntry
                        var found = false
                        while (entry != null) {
                            if (!entry.isDirectory && entry.name.endsWith("xray")) {
                                val outFile = CoreManagerInternal.coreBinary(context)
                                outFile.parentFile?.mkdirs()
                                FileOutputStream(outFile).use { out ->
                                    val buf = ByteArray(8 * 1024)
                                    while (true) {
                                        val n = zip.read(buf)
                                        if (n < 0) break
                                        out.write(buf, 0, n)
                                    }
                                }
                                found = true
                                break
                            }
                            entry = zip.nextEntry
                        }
                        if (!found) {
                            log(context, "[error] Zip did not contain xray binary")
                            continue
                        }
                    }
                    // mark executable
                    try {
                        android.system.Os.chmod(CoreManagerInternal.coreBinary(context).absolutePath, android.system.OsConstants.S_IRUSR or android.system.OsConstants.S_IWUSR or android.system.OsConstants.S_IXUSR)
                    } catch (_: Throwable) { CoreManagerInternal.coreBinary(context).setExecutable(true, true) }
                    log(context, "[info] Core downloaded and installed for abi=$abi")
                    return true
                } else {
                    log(context, "[error] HTTP ${conn.responseCode} for $u")
                }
            } catch (e: Exception) {
                log(context, "[error] Download failed from $u: ${e.message}")
            }
        }
        return false
    }
}
