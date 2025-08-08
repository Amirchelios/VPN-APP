package com.example.vpnapp.core

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import com.example.vpnapp.model.VpnProfile
import com.example.vpnapp.core.XrayConfigBuilder.buildConfigJson

object CoreManager {

    @Volatile
    private var coreProcess: Process? = null
    @Volatile private var tun2socksProcess: Process? = null

    // Versioning placeholder if needed later
    private const val XRAY_VERSION = "1.8.15"

    private fun getAbiTag(): String {
        val abi = Build.SUPPORTED_ABIS.firstOrNull()?.lowercase() ?: "arm64-v8a"
        return when {
            abi.contains("arm64") || abi.contains("aarch64") -> "arm64-v8a"
            abi.contains("armeabi-v7a") || abi.contains("armv7") -> "armeabi-v7a"
            abi.contains("x86_64") -> "x86_64"
            abi.contains("x86") -> "x86"
            else -> "arm64-v8a"
        }
    }

    private fun coreDir(context: Context): File = File(context.filesDir, "xray").apply { mkdirs() }
    private fun coreBinary(context: Context): File = File(coreDir(context), "xray")
    private fun configFile(context: Context): File = File(coreDir(context), "config.json")

    fun ensureCorePrepared(context: Context) {
        val binary = coreBinary(context)
        if (binary.exists()) return

        // Extract ABI-specific binary from assets: assets/cores/<abi>/xray
        val abi = getAbiTag()
        val assetPath = "cores/$abi/xray"
        val am = context.assets
        try {
            am.open(assetPath).use { input ->
                FileOutputStream(binary).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Xray binary not found in assets at $assetPath. Please add it.", e)
        }
        binary.setExecutable(true)
    }

    fun startCoreWithProfile(context: Context, profile: VpnProfile?) {
        if (coreProcess?.isAlive == true) return

        ensureCorePrepared(context)

        val config = buildConfigJson(profile)
        configFile(context).writeText(config)

        val pb = ProcessBuilder(listOf(coreBinary(context).absolutePath, "-config", configFile(context).absolutePath))
            .directory(coreDir(context))
            .redirectErrorStream(true)

        coreProcess = pb.start()
        attachLogging(coreProcess!!, context)

        // Optional tun2socks bridge if provided
        tryStartTun2Socks(context)
    }

    fun stopCore() {
        try {
            coreProcess?.destroy()
        } catch (_: Exception) {
        } finally {
            coreProcess = null
        }
        try {
            tun2socksProcess?.destroy()
        } catch (_: Exception) {
        } finally {
            tun2socksProcess = null
        }
    }

    private fun attachLogging(process: Process, context: Context) {
        val tag = "XrayCore"
        val logFile = File(coreDir(context), "xray.log")

        fun startReaderThread(name: String, stream: java.io.InputStream) {
            Thread({
                stream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        Log.i(tag, line)
                        try {
                            logFile.appendText(line + "\n")
                        } catch (_: Exception) { }
                    }
                }
            }, name).apply { isDaemon = true }.start()
        }

        startReaderThread("xray-stdout", process.inputStream)
        startReaderThread("xray-stderr", process.errorStream)
    }

    fun tryStartTun2Socks(context: Context, tunFd: Int) {
        val abi = getAbiTag()
        val assetPath = "cores/$abi/tun2socks"
        val dest = File(coreDir(context), "tun2socks")
        val am = context.assets
        try {
            am.open(assetPath).use { input ->
                FileOutputStream(dest).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                    }
                }
            }
            dest.setExecutable(true)
        } catch (_: Exception) {
            // No tun2socks in assets; skip
            return
        }

        // Start tun2socks to forward TUN FD to local socks at 127.0.0.1:10808
        try {
            val args = listOf(
                dest.absolutePath,
                "--loglevel", "info",
                "--tunfd", tunFd.toString(),
                "--socks-server-addr", "127.0.0.1:10808"
            )
            tun2socksProcess = ProcessBuilder(args)
                .directory(coreDir(context))
                .redirectErrorStream(true)
                .start()
            attachLogging(tun2socksProcess!!, context)
        } catch (_: Exception) {
        }
    }
}



