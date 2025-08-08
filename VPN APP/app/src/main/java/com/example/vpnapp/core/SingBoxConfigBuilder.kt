package com.example.vpnapp.core

import com.example.vpnapp.model.VpnProfile
import org.json.JSONArray
import org.json.JSONObject

object SingBoxConfigBuilder {

    fun buildConfigJson(profile: VpnProfile?): String {
        val outbounds = JSONArray()
        val inboundTun = JSONObject().apply {
            put("type", "tun")
            put("tag", "tun-in")
            put("mtu", 1500)
            put("auto_route", true)
            put("stack", "system")
        }

        val inbounds = JSONArray().put(inboundTun)

        if (profile != null) {
            outbounds.put(buildOutbound(profile))
        }

        // Default direct and block outbounds
        outbounds.put(JSONObject(mapOf("type" to "direct", "tag" to "direct")))
        outbounds.put(JSONObject(mapOf("type" to "block", "tag" to "block")))

        val route = JSONObject().apply {
            put("auto_detect_interface", true)
            put("auto_route", true)
            put("final", profile?.let { "proxy" } ?: "direct")
        }

        val root = JSONObject().apply {
            put("log", JSONObject(mapOf("level" to "info")))
            put("inbounds", inbounds)
            put("outbounds", outbounds)
            put("route", route)
        }
        return root.toString()
    }

    private fun buildOutbound(profile: VpnProfile): JSONObject {
        val common = JSONObject().apply {
            put("tag", "proxy")
            put("server", profile.serverHost ?: "")
            put("server_port", profile.serverPort ?: 443)
        }

        return when (profile.type.lowercase()) {
            "vless" -> JSONObject().apply {
                put("type", "vless")
                put("tag", common.getString("tag"))
                put("server", common.getString("server"))
                put("server_port", common.getInt("server_port"))
                put("uuid", profile.userId ?: "")
                put("flow", "")
                put("tls", JSONObject(mapOf("enabled" to true, "insecure" to false)))
            }
            "vmess" -> JSONObject().apply {
                put("type", "vmess")
                put("tag", common.getString("tag"))
                put("server", common.getString("server"))
                put("server_port", common.getInt("server_port"))
                put("uuid", profile.userId ?: "")
                put("security", "auto")
                put("tls", JSONObject(mapOf("enabled" to true, "insecure" to false)))
            }
            else -> JSONObject().apply {
                put("type", "direct")
                put("tag", "proxy")
            }
        }
    }
}


