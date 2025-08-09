package com.example.vpnapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vpnapp.R
import com.example.vpnapp.data.ProfileStore

class ProxiesListAdapter(private val onDataChanged: (() -> Unit)? = null) : RecyclerView.Adapter<ProxiesListAdapter.VH>() {
    private val items = mutableListOf<com.example.vpnapp.data.SavedProfile>()
    private var selectedId: String? = null

    fun refresh(context: android.content.Context) {
        try {
            items.clear()
            items.addAll(ProfileStore.list(context))
            selectedId = ProfileStore.getSelected(context)?.id
            notifyDataSetChanged()
            onDataChanged?.invoke()
        } catch (e: Exception) {
            // Handle error gracefully
            android.util.Log.e("ProxiesListAdapter", "Error refreshing", e)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_proxy, parent, false)
        return VH(v)
    }
    
    override fun getItemCount(): Int = items.size
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        try {
            val item = items[position]
            val isSelected = item.id == selectedId
            
            holder.title.text = item.name
            holder.subtitle.text = extractServerInfo(item.link)
            holder.txtPing.text = "-- ms"
            
            // Highlight selected item
            holder.itemView.isSelected = isSelected
            holder.itemView.alpha = if (isSelected) 1.0f else 0.7f
            
            holder.itemView.setOnClickListener {
                try {
                    ProfileStore.setSelected(holder.itemView.context, item.id)
                    refresh(holder.itemView.context)
                } catch (e: Exception) {
                    android.util.Log.e("ProxiesListAdapter", "Error selecting item", e)
                }
            }
            
            holder.btnDelete.setOnClickListener {
                try {
                    ProfileStore.remove(holder.itemView.context, item.id)
                    refresh(holder.itemView.context)
                } catch (e: Exception) {
                    android.util.Log.e("ProxiesListAdapter", "Error deleting item", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ProxiesListAdapter", "Error binding view", e)
        }
    }

    private fun extractServerInfo(link: String): String {
        return try {
            when {
                link.startsWith("vless://") -> {
                    val parts = link.removePrefix("vless://").split("@")
                    if (parts.size > 1) {
                        val serverPart = parts[1].split("?")[0]
                        "VLESS • $serverPart"
                    } else "VLESS"
                }
                link.startsWith("vmess://") -> "VMESS"
                link.startsWith("trojan://") -> "TROJAN"
                else -> "Unknown Protocol"
            }
        } catch (e: Exception) {
            "Invalid Link"
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val subtitle: TextView = v.findViewById(R.id.subtitle)
        val txtPing: TextView = v.findViewById(R.id.txtPing)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
    }
}


