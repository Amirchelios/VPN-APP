package com.example.vpnapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vpnapp.R

class ProxiesListAdapter : RecyclerView.Adapter<ProxiesListAdapter.VH>() {
    private val items = mutableListOf<String>()

    init {
        // placeholder list
        items.add("USA")
        items.add("Germany")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_proxy, parent, false)
        return VH(v)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.title.text = items[position]
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
    }
}


