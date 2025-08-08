package com.example.vpnapp

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.vpnapp.service.XrayVpnService
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.vpnapp.ui.MainPagerAdapter
import com.example.vpnapp.ui.AddServerDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = MainPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "خانه" else "پروکسی‌ها"
        }.attach()

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            // Open add/import dialog fragment
            AddServerDialogFragment().show(supportFragmentManager, "add_server")
        }
    }

    private fun requestVpnPermissionAndStart() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, REQ_VPN_PREPARE)
        } else {
            onActivityResult(REQ_VPN_PREPARE, Activity.RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_VPN_PREPARE && resultCode == Activity.RESULT_OK) {
            val link = "" // link is provided by selected profile later
            val intent = Intent(this, XrayVpnService::class.java).apply {
                action = XrayVpnService.ACTION_START
                putExtra(XrayVpnService.EXTRA_LINK, link)
            }
            ContextCompat.startForegroundService(this, intent)
        }
    }

    private fun stopVpn() {
        val intent = Intent(this, XrayVpnService::class.java).apply {
            action = XrayVpnService.ACTION_STOP
        }
        ContextCompat.startForegroundService(this, intent)
    }

    companion object {
        private const val REQ_VPN_PREPARE = 100
    }
}


