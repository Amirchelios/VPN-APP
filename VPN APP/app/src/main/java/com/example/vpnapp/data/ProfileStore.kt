package com.example.vpnapp.data

import android.content.Context
import android.content.SharedPreferences
import com.example.vpnapp.model.VpnProfile
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SavedProfile(
    val id: String,
    val name: String,
    val link: String
)

object ProfileStore {
    private const val PREFS = "profiles_store"
    private const val KEY_LIST = "profiles_json"
    private const val KEY_SELECTED = "selected_id"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun list(context: Context): List<SavedProfile> {
        val json = prefs(context).getString(KEY_LIST, "[]") ?: "[]"
        val arr = JSONArray(json)
        val out = mutableListOf<SavedProfile>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(SavedProfile(o.getString("id"), o.getString("name"), o.getString("link")))
        }
        return out
    }

    fun add(context: Context, link: String, parsed: VpnProfile?): SavedProfile {
        val id = UUID.randomUUID().toString()
        val name = parsed?.name ?: parsed?.type ?: "Profile"
        val item = SavedProfile(id, name, link)
        val cur = JSONArray(prefs(context).getString(KEY_LIST, "[]") ?: "[]")
        cur.put(JSONObject().apply {
            put("id", id); put("name", name); put("link", link)
        })
        prefs(context).edit().putString(KEY_LIST, cur.toString()).apply()
        return item
    }

    fun remove(context: Context, id: String) {
        val cur = JSONArray(prefs(context).getString(KEY_LIST, "[]") ?: "[]")
        val out = JSONArray()
        for (i in 0 until cur.length()) {
            val o = cur.getJSONObject(i)
            if (o.getString("id") != id) out.put(o)
        }
        val sel = prefs(context).getString(KEY_SELECTED, null)
        if (sel == id) prefs(context).edit().remove(KEY_SELECTED).apply()
        prefs(context).edit().putString(KEY_LIST, out.toString()).apply()
    }

    fun setSelected(context: Context, id: String) {
        prefs(context).edit().putString(KEY_SELECTED, id).apply()
    }

    fun getSelected(context: Context): SavedProfile? {
        val id = prefs(context).getString(KEY_SELECTED, null) ?: return null
        return list(context).firstOrNull { it.id == id }
    }
}


