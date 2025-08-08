package com.example.vpnapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vpnapp.R
import com.example.vpnapp.data.ProfileStore

class ProxiesListAdapter : RecyclerView.Adapter<ProxiesListAdapter.VH>() {
    private val items = mutableListOf<com.example.vpnapp.data.SavedProfile>()

    fun refresh(context: android.content.Context) {
        items.clear()
        items.addAll(ProfileStore.list(context))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_proxy, parent, false)
        return VH(v)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.name
        holder.itemView.setOnClickListener {
            ProfileStore.setSelected(holder.itemView.context, item.id)
        }
        holder.btnDelete.setOnClickListener {
            ProfileStore.remove(holder.itemView.context, item.id)
            refresh(holder.itemView.context)
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
    }
}


