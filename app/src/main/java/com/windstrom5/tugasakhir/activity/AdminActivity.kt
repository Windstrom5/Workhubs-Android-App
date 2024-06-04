package com.windstrom5.tugasakhir.activity

import android.content.Intent
import org.apache.commons.text.similarity.LevenshteinDistance
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.connection.SharedPreferencesManager
import com.windstrom5.tugasakhir.databinding.ActivityAdminBinding
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Perusahaan
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var tv: TextView
    private var bundle: Bundle? = null
    private var perusahaan: Perusahaan? = null
    private lateinit var tvnamaPerusahaan: TextView
    private lateinit var absen: CardView
    private lateinit var lembur: CardView
    private lateinit var dinas: CardView
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private lateinit var izin: CardView
    private lateinit var company: CardView
    private lateinit var cs: CardView
    private lateinit var back: ImageView
    private var admin: Admin? = null
    private lateinit var imageView: ImageView
    private lateinit var day: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var location: TextView
    private lateinit var quoteOfTheDay: TextView
//    private lateinit var weatherImage: ImageView
    private var weather:String?=null
    private var temp:String?=null
    private var currentLocation:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getBundle()
        imageView = binding.weatherIcon
        day = binding.dayText
        location = binding.location
        quoteOfTheDay = binding.dayText
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermissions()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val namaPerusahaan = perusahaan?.nama
        absen = binding.AbsensiCard
        lembur = binding.LemburCard
        dinas = binding.DinasCard
        izin = binding.IzinCard
        back = binding.backB
        back.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to Log Out?")
                .setPositiveButton("Yes") { _, _ ->
                    super.onBackPressed()
                    val sharedPreferencesManager = SharedPreferencesManager(this)
                    sharedPreferencesManager.clearUserData()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        company = binding.CompanyCard
        cs = binding.CustomerServiceCard
        val nama = admin?.nama
        val text = "© $currentYear $namaPerusahaan. \nAll rights reserved."
        tv = binding.courtesyNoticeTextView
        tv.text = text
        absen.setOnTouchListener { _, event -> handleCardTouch(absen, event, "AbsensiActivity") }
        lembur.setOnTouchListener { _, event -> handleCardTouch(lembur, event, "LemburActivity") }
        dinas.setOnTouchListener { _, event -> handleCardTouch(dinas, event, "DinasActivity") }
        izin.setOnTouchListener { _, event -> handleCardTouch(izin, event, "IzinActivity") }
        company.setOnTouchListener { _, event -> handleCardTouch(company, event, "CompanyActivity") }
        cs.setOnTouchListener { _, event -> handleCardTouch(cs, event, "CsActivity") }
    }

    private fun requestLocationPermissions() {
        val hasFineLocationPermission =
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission =
            checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

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
            getLastLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Location permissions are required to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val id = getDesaKelurahanFromLocation(latitude, longitude)
                    Log.d("Id Cuaca", id.toString())
                    id?.let {
                        getWeatherData(it)
                    }
                }
            }
    }

    private fun getDesaKelurahanFromLocation(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val desaKelurahan = address.subLocality ?: "Unknown"
                val kecamatan = address.locality ?: "Unknown"
                val kabupatenKota = address.subAdminArea ?: "Unknown"
                val province = address.adminArea ?: "Unknown"

//                Log.d("Location", "Desa/Kelurahan: $desaKelurahan, Kecamatan: $kecamatan, Kabupaten/Kota: $kabupatenKota, Province: $province")
                Toast.makeText(this, "Desa/Kelurahan: $desaKelurahan", Toast.LENGTH_SHORT).show()

                val wilayahJsonString = resources.openRawResource(R.raw.wilayah).bufferedReader().use { it.readText() }
                val wilayahArray = JSONArray(wilayahJsonString)
                return findMatchingLocation(wilayahArray, kecamatan, kabupatenKota, province)
            } else {
                Toast.makeText(this, "No address found for location.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to get desa/kelurahan from location.", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    private fun findMatchingLocation(
        wilayahArray: JSONArray,
        kecamatan: String,
        kabupatenKota: String,
        province: String
    ): String? {
        // Threshold for similarity score
        val threshold = 0.8// You can adjust this threshold as needed

        // Preprocess kabupatenKota string to remove prefixes
        val preprocessedKabupatenKota = kabupatenKota.replace("Kabupaten ", "").replace("Kota ", "")
        Log.d("Location1",preprocessedKabupatenKota)
        for (i in 0 until wilayahArray.length()) {
            val wilayahObject: JSONObject = wilayahArray.getJSONObject(i)
            val propinsi = wilayahObject.getString("propinsi")
            // Calculate similarity scores
            val kota = wilayahObject.getString("kota")
            val preprocessedKota = kota.replace("Kab. ", "").replace("Kota ", "")
            Log.d("Location1",preprocessedKota)
            val kecamatanMatchScore = similarityScore(kecamatan, wilayahObject.getString("kecamatan"))
            val kabupatenKotaMatchScore = similarityScore(preprocessedKabupatenKota, preprocessedKota)
            val provinceMatchScore = similarityScore(province, propinsi)

            // Check if any score exceeds the threshold
            if (kecamatanMatchScore >= threshold || kabupatenKotaMatchScore >= threshold || provinceMatchScore >= threshold) {
                val id = wilayahObject.getString("id")
                val kota3 = wilayahObject.getString("kota")
                val preprocessedKota = kota3.replace("Kabupaten ", "").replace("Kota ", "")
                currentLocation = preprocessedKota + ", "+ province
                Log.d("Location2", "Partial match found! ID: $id")
                return id
            }
        }

        // If no match found, return null
        return null
    }
    private fun similarityScore(str1: String, str2: String): Double {
        val set1 = str1.toSet()
        val set2 = str2.toSet()
        val intersectionSize = set1.intersect(set2).size
        val unionSize = set1.union(set2).size
        return intersectionSize.toDouble() / unionSize.toDouble()
    }

    private fun getWeatherData(id: String) {
        val url = "https://ibnux.github.io/BMKG-importer/cuaca/$id.json"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val currentTime = Calendar.getInstance().timeInMillis
                Log.d("Current Time", currentTime.toString())
                val weatherEntry = findWeatherForCurrentTime(response, currentTime)
                processWeatherData(weatherEntry)
                fetchHolidayData()
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Unable to fetch weather data.", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(jsonArrayRequest)
    }

    private fun findWeatherForCurrentTime(response: JSONArray, currentTime: Long): JSONObject? {
        for (i in 0 until response.length()) {
            val weatherEntry = response.getJSONObject(i)
            val timestampString = weatherEntry.getString("jamCuaca")
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(timestampString)?.time
            timestamp?.let {
                if (it > currentTime) {
                    val cuaca = weatherEntry.getInt("kodeCuaca")
                    Log.d("Code Cuaca",cuaca.toString())
                    when (cuaca) {
                        0 -> {
                            weather = "Clear Skies"
                            Glide.with(this)
                                .load(R.drawable.sunny)
                                .into(imageView)
                        }
                        1, 2 -> {
                            weather = "Partly Cloudy"
                            Glide.with(this)
                                .load(R.drawable.sunnycloudy)
                                .into(imageView)
                        }
                        3 -> {
                            weather = "Mostly Cloudy"
                            Glide.with(this)
                                .load(R.drawable.cloudy)
                                .into(imageView)
                        }
                        4 -> {
                            weather = "Overcast"
                            Glide.with(this)
                                .load(R.drawable.cloudy)
                                .into(imageView)
                        }
                        5 -> {
                            weather = "Haze"
                            Glide.with(this)
                                .load(R.drawable.cloudy)
                                .into(imageView)
                        }
                        10 -> {
                            weather = "Smoke"
                            Glide.with(this)
                                .load(R.drawable.cloudy)
                                .into(imageView)
                        }
                        45 -> {
                            weather = "Fog"
                            Glide.with(this)
                                .load(R.drawable.cloudy)
                                .into(imageView)
                        }
                        60 -> {
                            weather = "Light Rain"
                            Glide.with(this)
                                .load(R.drawable.rain)
                                .into(imageView)
                        }
                        61 -> {
                            weather = "Rain"
                            Glide.with(this)
                                .load(R.drawable.rain)
                                .into(imageView)
                        }
                        63 -> {
                            weather = "Heavy Rain"
                            Glide.with(this)
                                .load(R.drawable.rain)
                                .into(imageView)
                        }
                        80 -> {
                            weather = "Isolated Shower"
                            Glide.with(this)
                                .load(R.drawable.rain)
                                .into(imageView)
                        }
                        95, 97 -> {
                            weather = "Severe Thunderstorm"
                            Glide.with(this)
                                .load(R.drawable.storm)
                                .into(imageView)
                        }
                        else -> {
                            weather = "Unknown"
                            Glide.with(this)
                                .load(R.drawable.uknown)
                                .into(imageView)
                        }
                    }
                    Log.d("weather", weather!!)
                    return weatherEntry
                }
            }
        }
        return null
    }

    private fun processWeatherData(weatherEntry: JSONObject?) {
        weatherEntry?.let {
            val cuaca = it.getString("cuaca")
            val tempC = it.getString("tempC")
            val humidity = it.getString("humidity")
//            day.text = cuaca
            location.setText("Today Temperature Is "+ tempC + "°C At " + currentLocation)
            // Optionally, set an icon based on the weather condition
            // Glide.with(this).load(getWeatherIconUrl(cuaca)).into(imageView)
        }
    }

    private fun fetchHolidayData() {
        val url = "https://dayoffapi.vercel.app/api?year=2024"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                checkTodayHoliday(response)
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Unable to fetch holiday data.", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(jsonArrayRequest)
    }

    private fun checkTodayHoliday(holidays: JSONArray) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        var isHoliday = false

        for (i in 0 until holidays.length()) {
            val holiday = holidays.getJSONObject(i)
            if (holiday.getString("tanggal") == today) {
                day.text = holiday.getString("keterangan")
                isHoliday = true
                break
            }
        }
        weather?.let { Log.d("weather", it) }
        if (!isHoliday) {
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().time)
            val quotes = mapOf(
                "Monday" to "$weather Monday ${admin?.nama}! Start your week with a smile!",
                "Tuesday" to "It's a $weather Tuesday ${admin?.nama}! Keep going strong!",
                "Wednesday" to "Wonderful $weather Wednesday ${admin?.nama}! You're halfway there!",
                "Thursday" to "Thrilling $weather Thursday ${admin?.nama}! Almost the weekend!",
                "Friday" to "Fantastic $weather Friday ${admin?.nama}! Enjoy the day!",
                "Saturday" to "Superb $weather Saturday ${admin?.nama}! Have a great weekend!",
                "Sunday" to "Relaxing $weather Sunday ${admin?.nama}! Rest and recharge!"
            )
            day.text = dayName
            quoteOfTheDay.text = quotes[dayName] ?: "Have a great day!"
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to Log Out?")
            .setPositiveButton("Yes") { _, _ ->
                super.onBackPressed()
                val sharedPreferencesManager = SharedPreferencesManager(this)
                sharedPreferencesManager.clearUserData()
                startActivity(Intent(this, LoginActivity::class.java))
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun handleCardTouch(cardView: CardView, event: MotionEvent, activityName: String): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green))
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.whiteTextColor))
                when (activityName) {
                    "AbsensiActivity" -> {
                        val intent = Intent(this, AbsensiActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "LemburActivity" -> {
                        val intent = Intent(this, LemburActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "DinasActivity" -> {
                        val intent = Intent(this, DinasActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "IzinActivity" -> {
                        val intent = Intent(this, IzinActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "CompanyActivity" -> {
                        val intent = Intent(this, CompanyActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "CsActivity" -> {
                        val intent = Intent(this, LemburActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                }
            }
        }
        return true
    }

    private fun startActivityWithExtras(intent: Intent) {
        val userBundle = Bundle()
        userBundle.putParcelable("user", admin)
        userBundle.putParcelable("perusahaan", perusahaan)
        userBundle.putString("role", "Admin")
        intent.putExtra("data", userBundle)
        startActivity(intent)
    }

    private fun getBundle() {
        bundle = intent?.getBundleExtra("data")
        if (bundle != null) {
            bundle?.let {
                perusahaan = it.getParcelable("perusahaan")
                admin = it.getParcelable("user")
            }
        } else {
            Log.d("Error", "Bundle Not Found")
        }
    }
}
