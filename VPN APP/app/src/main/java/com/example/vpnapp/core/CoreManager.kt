package com.example.vpnapp.core

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import com.example.vpnapp.model.VpnProfile
import com.example.vpnapp.core.XrayConfigBuilder.buildConfigJson
import android.system.Os
import android.system.OsConstants

object CoreManager {

    @Volatile
    private var coreProcess: Process? = null
    @Volatile private var tun2socksProcess: Process? = null

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

    private fun coreDir(context: Context): File = context.getDir("bin", Context.MODE_PRIVATE).apply { mkdirs() }
    private fun coreBinary(context: Context): File = File(coreDir(context), "xray")
    private fun configFile(context: Context): File = File(coreDir(context), "config.json")
    private fun logFile(context: Context): File = File(coreDir(context), "xray.log")

    private fun makeExecutable(file: File) {
        try {
            // 0755
            Os.chmod(
                file.absolutePath,
                OsConstants.S_IRUSR or OsConstants.S_IWUSR or OsConstants.S_IXUSR or
                    OsConstants.S_IRGRP or OsConstants.S_IXGRP or
                    OsConstants.S_IROTH or OsConstants.S_IXOTH
            )
        } catch (e: Throwable) {
            try { file.setExecutable(true, false) } catch (_: Exception) {}
        }
    }

    private fun diagElf(file: File, context: Context) {
        try {
            val bytes = file.inputStream().use { it.readNBytes(8) }
            val isElf = bytes.size >= 4 && bytes[0] == 0x7F.toByte() && bytes[1].toInt() == 'E'.code && bytes[2].toInt() == 'L'.code && bytes[3].toInt() == 'F'.code
            val cls = if (bytes.size > 4) bytes[4].toInt() else -1 // 1=32,2=64
            val abiList = Build.SUPPORTED_ABIS.joinToString(",")
            logFile(context).appendText("[diag] ELF=${isElf} class=${cls} size=${file.length()} abis=${abiList} exec=${file.canExecute()} path=${file.absolutePath}\n")
        } catch (_: Exception) {}
    }

    fun ensureCorePrepared(context: Context) {
        val binary = coreBinary(context)
        if (!binary.exists()) {
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
        }
        makeExecutable(binary)
        diagElf(binary, context)
        try {
            val diag = "xray at ${binary.absolutePath} exists=${binary.exists()} size=${binary.length()} canExec=${binary.canExecute()}"
            logFile(context).appendText("[diag] $diag\n")
        } catch (_: Exception) {}
    }

    fun startCoreWithProfile(context: Context, profile: VpnProfile?): Boolean {
        if (coreProcess?.isAlive == true) return true
        ensureCorePrepared(context)

        try {
            val config = buildConfigJson(profile)
            configFile(context).writeText(config)
        } catch (e: Exception) {
            Log.e("CoreManager", "Failed writing config", e)
            try { logFile(context).appendText("[error] Failed writing config: ${e.message}\n") } catch (_: Exception) {}
            return false
        }

        return try {
            val pb = ProcessBuilder(listOf(coreBinary(context).absolutePath, "-config", configFile(context).absolutePath))
                .directory(coreDir(context))
                .redirectErrorStream(true)

            coreProcess = pb.start()
            attachLogging(coreProcess!!, context)
            true
        } catch (e: Exception) {
            Log.e("CoreManager", "Failed starting xray core", e)
            try { logFile(context).appendText("[error] Failed starting xray: ${e.message}\n") } catch (_: Exception) {}
            coreProcess = null
            false
        }
    }

    fun stopCore() {
        try { coreProcess?.destroy() } catch (_: Exception) {} finally { coreProcess = null }
        try { tun2socksProcess?.destroy() } catch (_: Exception) {} finally { tun2socksProcess = null }
    }

    private fun attachLogging(process: Process, context: Context) {
        val tag = "XrayCore"
        val logFile = logFile(context)

        fun startReaderThread(name: String, stream: java.io.InputStream) {
            Thread({
                stream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        Log.i(tag, line)
                        try { logFile.appendText(line + "\n") } catch (_: Exception) { }
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
            makeExecutable(dest)
            try { logFile(context).appendText("[diag] tun2socks at ${dest.absolutePath} canExec=${dest.canExecute()}\n") } catch (_: Exception) {}
        } catch (_: Exception) {
            return
        }

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
        } catch (e: Exception) {
            try { logFile(context).appendText("[error] Failed starting tun2socks: ${e.message}\n") } catch (_: Exception) {}
        }
    }
}



