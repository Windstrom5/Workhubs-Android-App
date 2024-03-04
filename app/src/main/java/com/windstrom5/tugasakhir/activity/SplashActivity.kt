package com.windstrom5.tugasakhir.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.connection.SharedPreferencesManager
import com.windstrom5.tugasakhir.model.login_session
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class SplashActivity : AppCompatActivity() {
    private val splashTimeOut: Long = 2000 // 2 seconds
    // Define LOCATION_PERMISSION_REQUEST_CODE here
    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        requestLocationPermissions()
//        val sharedPreferencesManager = SharedPreferencesManager(this)
//        val savedSession = sharedPreferencesManager.getSession()
//        if(savedSession!= null && checkSession(savedSession) == true){
//            redirectToActivity(savedSession)
//        }else{
//            Handler().postDelayed({
//                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
//                startActivity(intent)
//                finish()
//            }, splashTimeOut)
//        }
    }
    private fun requestLocationPermissions() {
        // Check whether your app already has the permissions.
        val hasFineLocationPermission =
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission =
            checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        // If permissions are not granted, request them
        if (!hasFineLocationPermission || !hasCoarseLocationPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions already granted, continue with the splash
            continueWithSplash()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Permission denied, check if "Don't ask again" is selected
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                ) {
                    // User denied permission but didn't select "Don't ask again"
                    // Show a toast or handle it accordingly
                    showToast("Location permission is required.")
                    finish()
                } else {
                    // User denied permission and selected "Don't ask again"
                    // Show a toast and direct the user to app settings
                    showToastWithDelay("Please enable Location permission.")
                }
            } else {
                // Permission granted, continue with the splash
                continueWithSplash()
            }
        }
    }
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun showToast(message: String) {
        MotionToast.createToast(
            this,
            "Error",
            message,
            MotionToastStyle.ERROR,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
        )
    }
    private fun showToastWithDelay(message: String) {
        showToast(message)
        Handler().postDelayed({
            openAppSettings()
        }, MotionToast.LONG_DURATION)
    }
    private fun continueWithSplash() {
        val sharedPreferencesManager = SharedPreferencesManager(this)
        val savedSession = sharedPreferencesManager.getSession()

        if (savedSession != null && checkSession(savedSession)) {
            redirectToActivity(savedSession)
        } else {
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