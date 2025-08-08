package com.example.vpnapp.model

data class VpnProfile(
    val name: String,
    val type: String,
    val uri: String,
    val serverHost: String? = null,
    val serverPort: Int? = null,
    val userId: String? = null,
    val method: String? = null,
    val password: String? = null
)


