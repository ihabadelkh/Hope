package com.ihabAKH.hope.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.ihabAKH.hope.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        hideStatusBar()
        delay()
    }

    private fun goToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun delay() {
        Handler().postDelayed({
            goToLoginActivity()
        }, 3000)
    }

    private fun hideStatusBar() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}