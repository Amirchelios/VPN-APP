package com.example.vpnapp.core

import com.example.vpnapp.model.VpnProfile
import org.json.JSONArray
import org.json.JSONObject

object XrayConfigBuilder {

    fun buildConfigJson(profile: VpnProfile?): String {
        val log = JSONObject().apply {
            put("loglevel", "info")
        }

        // Inbound: local SOCKS to be used by tun2socks later (placeholder)
        val inbounds = JSONArray().apply {
            put(
                JSONObject().apply {
                    put("tag", "socks-in")
                    put("port", 10808)
                    put("listen", "127.0.0.1")
                    put("protocol", "socks")
                    put("settings", JSONObject().apply { put("udp", true) })
                }
            )
        }

        val outbounds = JSONArray()
        if (profile != null) {
            outbounds.put(buildOutbound(profile))
        }
        outbounds.put(JSONObject().apply { put("protocol", "freedom"); put("tag", "direct") })
        outbounds.put(JSONObject().apply { put("protocol", "blackhole"); put("tag", "block") })

        val routing = JSONObject().apply {
            put("domainStrategy", "AsIs")
            put("rules", JSONArray())
        }

        val root = JSONObject().apply {
            put("log", log)
            put("inbounds", inbounds)
            put("outbounds", outbounds)
            put("routing", routing)
        }
        return root.toString()
    }

    private fun buildOutbound(profile: VpnProfile): JSONObject {
        return when (profile.type.lowercase()) {
            "vless" -> JSONObject().apply {
                put("tag", "proxy")
                put("protocol", "vless")
                put("settings", JSONObject().apply {
                    put("vnext", JSONArray().apply {
                        put(JSONObject().apply {
                            put("address", profile.serverHost ?: "")
                            put("port", profile.serverPort ?: 443)
                            put("users", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("id", profile.userId ?: "")
                                    put("encryption", "none")
                                })
                            })
                        })
                    })
                })
                put("streamSettings", buildStreamSettings(profile))
            }
            "vmess" -> JSONObject().apply {
                put("tag", "proxy")
                put("protocol", "vmess")
                put("settings", JSONObject().apply {
                    put("vnext", JSONArray().apply {
                        put(JSONObject().apply {
                            put("address", profile.serverHost ?: "")
                            put("port", profile.serverPort ?: 443)
                            put("users", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("id", profile.userId ?: "")
                                    put("security", "auto")
                                })
                            })
                        })
                    })
                })
                put("streamSettings", buildStreamSettings(profile))
            }
            else -> JSONObject().apply { put("protocol", "freedom"); put("tag", "proxy") }
        }
    }

    private fun buildStreamSettings(profile: VpnProfile): JSONObject {
        val network = (profile.transport ?: "tcp").lowercase()
        val tlsEnabled = profile.tlsEnabled ?: false
        val sni = profile.sni ?: profile.serverHost ?: ""
        val insecure = profile.tlsInsecure ?: false

        val obj = JSONObject().apply { put("network", network) }

        if (tlsEnabled) {
            obj.put("security", "tls")
            obj.put("tlsSettings", JSONObject().apply {
                put("serverName", sni)
                put("allowInsecure", insecure)
                if (!profile.alpn.isNullOrBlank()) put("alpn", JSONArray().put(profile.alpn))
            })
        } else {
            obj.put("security", "none")
        }

        when (network) {
            "ws" -> obj.put("wsSettings", JSONObject().apply {
                if (!profile.path.isNullOrBlank()) put("path", profile.path)
                if (!profile.hostHeader.isNullOrBlank()) put("headers", JSONObject().apply { put("Host", profile.hostHeader) })
            })
            "tcp" -> if (profile.headerType == "http") {
                obj.put("tcpSettings", JSONObject().apply {
                    put("header", JSONObject().apply {
                        put("type", "http")
                        if (!profile.hostHeader.isNullOrBlank()) put("request", JSONObject().apply {
                            put("headers", JSONObject().apply { put("Host", JSONArray().put(profile.hostHeader)) })
                            put("path", JSONArray().put(profile.path ?: "/"))
                        })
                    })
                })
            }
        }
        return obj
    }
}


