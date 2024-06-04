package com.windstrom5.tugasakhir.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.connection.ApiService
import com.windstrom5.tugasakhir.connection.SharedPreferencesManager
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import com.windstrom5.tugasakhir.model.login_session
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.sql.Time
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SplashActivity : AppCompatActivity() {
    private var perusahaanList: List<Perusahaan> = emptyList()
    private val splashTimeOut: Long = 2000 // 2 seconds
    private lateinit var logoImageView: ImageView
    // Define LOCATION_PERMISSION_REQUEST_CODE here
    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        logoImageView = findViewById(R.id.logoImageView)
        Glide.with(this)
            .load(R.drawable.logo)
            .into(logoImageView)
        Log.d("Halo","hlao")
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

        private fun fetchDataFromApi() {
        val url = "http://192.168.1.3:8000/api/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getPerusahaan()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        try {
                            val responseData = JSONObject(responseBody.string())
                            val perusahaanArray = responseData.getJSONArray("perusahaan")

                            val newPerusahaanList = mutableListOf<Perusahaan>()
                            for (i in 0 until perusahaanArray.length()) {
                                val perusahaanObject = perusahaanArray.getJSONObject(i)

                                val id = perusahaanObject.getInt("id")
                                val nama = perusahaanObject.getString("nama")
                                val latitude = perusahaanObject.getDouble("latitude")
                                val longitude = perusahaanObject.getDouble("longitude")
                                val jam_masukStr = perusahaanObject.getString("jam_masuk")
                                val jam_keluarStr = perusahaanObject.getString("jam_keluar")
                                val jam_masuk = convertStringToTime(jam_masukStr)
                                val jam_keluar = convertStringToTime(jam_keluarStr)
                                val batasAktif = perusahaanObject.getString("batas_aktif")
                                val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val javaUtilDate = dateParser.parse(batasAktif)
                                val sqlDate = java.sql.Date(javaUtilDate.time)
                                val logo = perusahaanObject.getString("logo")
                                val secretKey = perusahaanObject.getString("secret_key")

                                // Create Perusahaan object
                                val perusahaan = Perusahaan(
                                    id,
                                    nama,
                                    latitude,
                                    longitude,
                                    jam_masuk,
                                    jam_keluar,
                                    sqlDate,
                                    logo,
                                    secretKey
                                )
                                newPerusahaanList.add(perusahaan)
                            }

                            // Now you can pass newPerusahaanList to the next activity
                            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                            intent.putExtra("perusahaanList", ArrayList(newPerusahaanList))
                            startActivity(intent)

                        } catch (e: JSONException) {
                            Log.e("FetchDataError", "Error parsing JSON: ${e.message}")
                        } catch (e: ParseException) {
                            Log.e("FetchDataError", "Error parsing date: ${e.message}")
                        }
                    }
                } else {
                    runOnUiThread{
                        MotionToast.createToast(this@SplashActivity,
                            "Error",
                            "Gagal Menyambungkan Ke Server",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this@SplashActivity,
                                R.font.ralewaybold))
                    }
                    Log.e("FetchDataError", "Failed to fetch data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle network failures
                Log.e("FetchDataError", "Failed to fetch data: ${t.message}")
                runOnUiThread{
                    MotionToast.createToast(this@SplashActivity,
                        "Error",
                        "Gagal Menyambungkan Ke Server",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this@SplashActivity,
                            R.font.ralewaybold))
                    finish() // Close the activity
                }
            }
        })
    }
    fun convertStringToTime(timeStr: String): Time {
        val sdf = SimpleDateFormat("HH:mm:ss")
        val date: Date = sdf.parse(timeStr)
        return Time(date.time)
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
            ResourcesCompat.getFont(this,R.font.ralewaybold)
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
        val savedAdmin: Admin? = sharedPreferencesManager.getAdmin()
        val savedPekerja: Pekerja? = sharedPreferencesManager.getPekerja()
        val savedPerusahaan: Perusahaan? = sharedPreferencesManager.getPerusahaan()
        if (savedAdmin != null || savedPekerja != null) {
            redirectToActivity(savedPerusahaan,savedAdmin,savedPekerja)
        } else {
//            fetchDataFromApi()
        }
    }
    private fun redirectToActivity(perusahaan: Perusahaan?, admin: Admin?, pekerja: Pekerja?) {
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val imageUrl =
            "http://192.168.1.5:8000/storage/${perusahaan?.logo}" // Replace with your Laravel image URL

        Glide.with(this)
            .load(imageUrl) // Assuming savedPerusahaan has a 'logo' field containing the URL
            .into(logoImageView)

        if (admin != null) {
            Handler().postDelayed({
                val intent = Intent(this@SplashActivity, AdminActivity::class.java)
                val userBundle = Bundle()
                userBundle.putParcelable("user", admin)
                userBundle.putParcelable("perusahaan", perusahaan)
                intent.putExtra("data", userBundle)
                startActivity(intent)
                finish()
            }, splashTimeOut)
        } else {
            Handler().postDelayed({
                val intent = Intent(this@SplashActivity, UserActivity::class.java)
                val userBundle = Bundle()
                userBundle.putParcelable("user", pekerja)
                userBundle.putParcelable("perusahaan", perusahaan)
                intent.putExtra("data", userBundle)
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