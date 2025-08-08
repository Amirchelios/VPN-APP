package com.example.vpnapp.parser

import android.util.Base64
import com.example.vpnapp.model.VpnProfile
import org.json.JSONObject
import java.net.URI

object LinkParser {

    fun parseLink(link: String?): VpnProfile? {
        if (link.isNullOrBlank()) return null
        return when {
            link.startsWith("vmess://", true) -> parseVmess(link)
            link.startsWith("vless://", true) -> parseVless(link)
            else -> null
        }
    }

    private fun parseVmess(link: String): VpnProfile? {
        return try {
            val encoded = link.removePrefix("vmess://")
            val jsonStr = String(Base64.decode(encoded, Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE))
            val json = JSONObject(jsonStr)
            val host = json.optString("add")
            val port = json.optInt("port")
            val id = json.optString("id")
            val name = json.optString("ps", "VMess")
            VpnProfile(
                name = name,
                type = "vmess",
                uri = link,
                serverHost = host,
                serverPort = port,
                userId = id
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseVless(link: String): VpnProfile? {
        return try {
            val uri = URI(link)
            val userInfo = uri.userInfo ?: ""
            val host = uri.host ?: ""
            val port = if (uri.port != -1) uri.port else 443
            val name = "VLESS"
            VpnProfile(
                name = name,
                type = "vless",
                uri = link,
                serverHost = host,
                serverPort = port,
                userId = userInfo
            )
        } catch (_: Exception) {
            null
        }
    }
}


