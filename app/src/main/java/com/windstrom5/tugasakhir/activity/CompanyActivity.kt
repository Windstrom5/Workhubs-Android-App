package com.windstrom5.tugasakhir.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.SubscriptionEventListener
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.adapter.ListAnggotaAdapter
import com.windstrom5.tugasakhir.connection.ApiResponse
import com.windstrom5.tugasakhir.connection.ApiService
import com.windstrom5.tugasakhir.connection.WorkHubs
import com.windstrom5.tugasakhir.databinding.ActivityCompanyBinding
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import kotlinx.coroutines.Job
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompanyActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCompanyBinding
    private var admin : Admin? = null
    private var pekerja : Pekerja? = null
    private var bundle: Bundle? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var perusahaan : Perusahaan? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListAnggotaAdapter
    private lateinit var role:String
    private lateinit var requestQueue: RequestQueue
    private lateinit var addPekerja: Button
    private var job: Job? = null
    private var fetchRunnable: Runnable? = null
    private val handler = Handler()
    private val pollingInterval = 2000L // Polling interval in milliseconds
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addPekerja = binding.addPekerja
        getBundle()
        recyclerView = findViewById(R.id.recyclerViewPekerja)
        adapter =
            perusahaan?.let { ListAnggotaAdapter(mutableListOf(), mutableListOf(), it,role,this@CompanyActivity) }!!// Initialize with empty lists
        recyclerView.adapter = adapter
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager
        requestQueue = Volley.newRequestQueue(this)
        perusahaan?.let { fetchDataFromApi(it.nama) }
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            perusahaan?.let { fetchDataFromApi(it.nama) }
        }
        addPekerja.setOnClickListener{
            val intent = Intent(this, RegisterPekerjaActivity::class.java)
            val userBundle = Bundle()
            userBundle.putParcelable("user", admin)
            userBundle.putParcelable("perusahaan", perusahaan)
            userBundle.putString("role", "Admin")
            intent.putExtra("data", userBundle)
            startActivity(intent)
        }

    }

    private fun fetchDataFromApi(namaPerusahaan: String) {
        val url = "http://192.168.1.6:8000/api/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
//        fetchRunnable = object : Runnable {
//            override fun run() {
                val call = apiService.getDataPekerja(namaPerusahaan)
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            response.body()?.let { responseBody ->
                                try {
                                    val responseData = JSONObject(responseBody.string())
                                    // Handle admin and pekerja as JSON objects
                                    val adminArray = responseData.getJSONArray("admin")
                                    val adminList = parseAdminList(adminArray)
                                    val pekerjaArray = responseData.getJSONArray("pekerja")
                                    val pekerjaList = parsePekerjaList(pekerjaArray)
                                    adapter.updateData(pekerjaList.toMutableList(), adminList.toMutableList())
                                    swipeRefreshLayout.isRefreshing = false
                                } catch (e: JSONException) {
                                    Log.e("FetchDataError", "Error parsing JSON: ${e.message}")
                                }
                            }
                        } else {
                            // Handle unsuccessful response
                            Log.e("FetchDataError", "Failed to fetch data: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // Handle network failures
                        Log.e("FetchDataError", "Failed to fetch data: ${t.message}")
                    }
                })
    }

    private fun parsePerusahaan(perusahaanObject: JSONObject): Perusahaan {
        // Implement parsing logic for perusahaan data
        val batasAktif = perusahaanObject.getString("batas_aktif")
        val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val javaUtilDate = dateParser.parse(batasAktif)

        // Convert java.util.Date to java.sql.Date
        val sqlDate = java.sql.Date(javaUtilDate.time)
        return Perusahaan(
            perusahaanObject.getInt("id"),
            perusahaanObject.getString("nama"),
            perusahaanObject.getDouble("latitude"),
            perusahaanObject.getDouble("longitude"),
            convertStringToTime(perusahaanObject.getString("jam_masuk")),
            convertStringToTime(perusahaanObject.getString("jam_keluar")),
            sqlDate,
            perusahaanObject.getString("logo"),
            perusahaanObject.getString("secret_key")
        )
    }

    @SuppressLint("SimpleDateFormat")
    fun convertStringToTime(timeStr: String): Time {
        val sdf = SimpleDateFormat("HH:mm:ss")
        val date: Date = sdf.parse(timeStr)
        return Time(date.time)
    }

    private fun parseAdminList(adminArray: JSONArray): List<Admin> {
        // Implement parsing logic for admin data
        val adminList = mutableListOf<Admin>()
        for (i in 0 until adminArray.length()) {
            val adminObject = adminArray.getJSONObject(i)
            adminList.add(
                Admin(
                    adminObject.getInt("id"),
                    adminObject.getInt("id_perusahaan"),
                    adminObject.getString("email"),
                    adminObject.getString("password"),
                    adminObject.getString("nama"),
                    parseDate(adminObject.getString("tanggal_lahir")),
                    adminObject.getString("profile")
                )
            )
        }
        return adminList
    }

    private fun parsePekerjaList(pekerjaArray: JSONArray): List<Pekerja> {
        // Implement parsing logic for pekerja data
        val pekerjaList = mutableListOf<Pekerja>()
        for (i in 0 until pekerjaArray.length()) {
            val pekerjaObject = pekerjaArray.getJSONObject(i)
            pekerjaList.add(
                Pekerja(
                    pekerjaObject.getInt("id"),
                    pekerjaObject.getInt("id_perusahaan"),
                    pekerjaObject.getString("email"),
                    pekerjaObject.getString("password"),
                    pekerjaObject.getString("nama"),
                    parseDate(pekerjaObject.getString("tanggal_lahir")),
                    pekerjaObject.getString("profile")
                )
            )
        }
        return pekerjaList
    }
    private fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }
    // Don't forget to cancel the requests in onDestroy to avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll(this)
        fetchRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
    private fun getBundle() {
        bundle = intent?.getBundleExtra("data")
        if (bundle != null) {
            bundle?.let {
                perusahaan = it.getParcelable("perusahaan")
                role = it.getString("role").toString()
                if(role == "Admin"){
                    admin = it.getParcelable("user")
                    addPekerja.visibility = View.VISIBLE
                }else{
                    pekerja = it.getParcelable("user")
                    addPekerja.visibility = View.GONE
                }
                val imageUrl =
                    "http://192.168.1.6:8000/storage/${perusahaan?.logo}" // Replace with your Laravel image URL
                val profileImageView = binding.companyLogoImageView
                val text = binding.headerText
                text.setText("List Anggota \nPerusahaan ${perusahaan?.nama}")
                Glide.with(this)
                    .load(imageUrl)
                    .into(profileImageView)
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        // Remove any pending fetchRunnable instances from the handler
        fetchRunnable?.let {
            handler.removeCallbacks(it)
        }
        if(pekerja != null){
            val userBundle = Bundle()
            val intent = Intent(this, UserActivity::class.java)
            userBundle.putParcelable("user", pekerja)
            userBundle.putParcelable("perusahaan", perusahaan)
            intent.putExtra("data", userBundle)
            startActivity(intent)
        }else{
            val userBundle = Bundle()
            val intent = Intent(this, AdminActivity::class.java)
            userBundle.putParcelable("user", admin)
            userBundle.putParcelable("perusahaan", perusahaan)
            intent.putExtra("data", userBundle)
            startActivity(intent)
        }
    }
}