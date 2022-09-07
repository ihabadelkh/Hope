package com.ihabAKH.hope.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ihabAKH.hope.R
import com.ihabAKH.hope.adapter.ViewPagerAdapter
import com.ihabAKH.hope.databinding.ActivityMainBinding
import com.ihabAKH.hope.fragment.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpTabs()
        signOut()
    }

    private fun setUpTabs() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(HomeFragment(), "")
        adapter.addFragment(RequestsFragment(), "")
        adapter.addFragment(ProfileFragment(), "")
        adapter.addFragment(NotificationFragment(), "")
        adapter.addFragment(MenuFragment(), "")
        binding.viewPager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.tabs.getTabAt(0)!!.setIcon(R.drawable.home)
        binding.tabs.getTabAt(1)!!.setIcon(R.drawable.friend_request)
        binding.tabs.getTabAt(2)!!.setIcon(R.drawable.profile)
        binding.tabs.getTabAt(3)!!.setIcon(R.drawable.notification)
        binding.tabs.getTabAt(4)!!.setIcon(R.drawable.menu)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun signOut() {
        mAuth = Firebase.auth
        binding.powerIcon.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.custom_alert_dialog)
            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.round_corners))
            val yes = dialog.findViewById<TextView>(R.id.tvYes)
            val no = dialog.findViewById<TextView>(R.id.tvNo)
            yes.setOnClickListener {
                mAuth.signOut()
                goToLoginActivity()
            }
            no.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun goToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}