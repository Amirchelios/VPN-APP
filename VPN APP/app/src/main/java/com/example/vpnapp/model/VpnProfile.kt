package com.example.vpnapp.model

data class VpnProfile(
    val name: String,
    val type: String,
    val uri: String,
    val serverHost: String? = null,
    val serverPort: Int? = null,
    val userId: String? = null,
    val method: String? = null,
    val password: String? = null,
    // Transport/TLS extras
    val transport: String? = null, // tcp, ws, grpc
    val path: String? = null,
    val hostHeader: String? = null,
    val headerType: String? = null,
    val sni: String? = null,
    val alpn: String? = null,
    val tlsEnabled: Boolean? = null,
    val tlsInsecure: Boolean? = null,
    val flow: String? = null
)


