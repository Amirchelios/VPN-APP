package com.example.vpnapp.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment =
        if (position == 0) HomeFragment() else ProxiesFragment()
}


