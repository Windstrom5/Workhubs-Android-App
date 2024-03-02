package com.windstrom5.tugasakhir

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.windstrom5.tugasakhir.connection.SharedPreferencesManager
import com.windstrom5.tugasakhir.model.login_session

class SplashActivity : AppCompatActivity() {
    private val splashTimeOut: Long = 2000 // 2 seconds
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val sharedPreferencesManager = SharedPreferencesManager(this)
        val savedSession = sharedPreferencesManager.getSession()
        if(savedSession!= null && checkSession(savedSession) == true){
            redirectToActivity(savedSession)
        }else{
            Handler().postDelayed({
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, splashTimeOut)
        }
    }
    private fun redirectToActivity(session: login_session) {
        val sharedPreferencesManager = SharedPreferencesManager(this)
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)

        // Load logo image using Glide
        Glide.with(this)
            .load(sharedPreferencesManager.getPerusahaan()?.logo) // Assuming savedPerusahaan has a 'logo' field containing the URL
            .into(logoImageView)

        if (session.role == "Admin") {
            Handler().postDelayed({
                val savedAdmin = sharedPreferencesManager.getAdmin()
                val intent = Intent(this@SplashActivity, AdminActivity::class.java)
                val userBundle = Bundle()
                userBundle.putParcelable("user", savedAdmin)
                intent.putExtra("user_bundle", userBundle)
                startActivity(intent)
                finish()
            }, splashTimeOut)
        } else {
            Handler().postDelayed({
                val savedPekerja = sharedPreferencesManager.getPekerja()
                val intent = Intent(this@SplashActivity, UserActivity::class.java)
                val userBundle = Bundle()
                userBundle.putParcelable("user", savedPekerja)
                intent.putExtra("user_bundle", userBundle)
                startActivity(intent)
                finish()
            }, splashTimeOut)
        }
    }

    private fun checkSession(session: login_session):Boolean{
        val currentTime = System.currentTimeMillis()
        val sessionCreateTime = session.create_at.time
        val sessionDurationMillis = 8 * 60 * 60 * 1000 // 8 hours in milliseconds
        return currentTime - sessionCreateTime < sessionDurationMillis
    }
}