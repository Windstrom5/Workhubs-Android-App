package com.example.tugasakhir

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import com.example.tugasakhir.connection.SharedPreferencesManager
import com.example.tugasakhir.model.login_session

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
    private fun redirectToActivity(Session: login_session) {
        val sharedPreferencesManager = SharedPreferencesManager(this)
        // Determine which activity to redirect based on user's data
        val savedPerusahaan = sharedPreferencesManager.getPerusahaan()
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        savedPerusahaan?.let { perusahaan ->
            // Decode the byte array into a Bitmap
            val logoBitmap = BitmapFactory.decodeByteArray(perusahaan.logo, 0, perusahaan.logo.size)

            // Set the Bitmap to the ImageView
            logoImageView.setImageBitmap(logoBitmap)
        }
        if (Session.role == "Admin") {
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