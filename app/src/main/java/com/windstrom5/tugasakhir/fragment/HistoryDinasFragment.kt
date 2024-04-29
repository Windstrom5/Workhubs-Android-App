package com.windstrom5.tugasakhir.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.adapter.DinasAdapter
import com.windstrom5.tugasakhir.connection.ApiService
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.DinasItem
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import com.windstrom5.tugasakhir.model.historyDinas
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import okhttp3.ResponseBody
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryDinasFragment : Fragment() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: DinasAdapter
    private var perusahaan : Perusahaan? = null
    private var admin : Admin? = null
    private var pekerja : Pekerja? = null
    private var role : String? = null
    private var fetchRunnable: Runnable? = null
    private val handler = Handler()
    private val pollingInterval = 2000L
    private val statusWithDinasList = mutableListOf<historyDinas>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_dinas, container, false)
        getBundle()
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        if(admin != null){
            perusahaan?.let { fetchDataPerusahaanFromApi(it.nama) }
            swipeRefreshLayout.setOnRefreshListener {
                perusahaan?.let { fetchDataPerusahaanFromApi(it.nama) }
            }
        }else{
            perusahaan?.let { pekerja?.let { it1 -> fetchDataPekerjaFromApi(it.nama, it1.nama) } }
            swipeRefreshLayout.setOnRefreshListener {
                perusahaan?.let { pekerja?.let { it1 -> fetchDataPekerjaFromApi(it.nama, it1.nama) } }
            }
        }
        return view
    }
    private fun fetchDataPekerjaFromApi(namaPerusahaan: String,nama_pekerja: String) {
        val url = "http://192.168.1.4:8000/api/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getDataDinasPekerja(namaPerusahaan,nama_pekerja)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        try {
                            val jsonResponse = responseBody.string()
                            val responseData = JSONObject(jsonResponse)
                            val dataArray = responseData.getJSONArray("data")
                            // Initialize a map to hold status with corresponding Dinas list
                            val statusWithDinasMap = mutableMapOf<String, MutableList<DinasItem>>()

                            for (i in 0 until dataArray.length()) {
                                val jsonObject = dataArray.getJSONObject(i)
                                val status = jsonObject.getString("status")
                                val tanggalBerangkatString =
                                    jsonObject.getString("tanggal_berangkat")
                                val tanggalPulangString = jsonObject.getString("tanggal_pulang")
                                val tanggalBerangkat = parseDate(tanggalBerangkatString)
                                val tanggalPulang = parseDate(tanggalPulangString)
                                val dinas = DinasItem(
                                    jsonObject.getInt("id"),
                                    jsonObject.getString("nama_pekerja"),
                                    jsonObject.getString("nama_perusahaan"),
                                    jsonObject.getString("tujuan"),
                                    tanggalBerangkat,
                                    tanggalPulang,
                                    jsonObject.getString("kegiatan"),
                                    jsonObject.getString("bukti"),
                                    jsonObject.getString("status")
                                )

                                // Add dinas to corresponding status list
                                if (statusWithDinasMap.containsKey(status)) {
                                    statusWithDinasMap[status]?.add(dinas)
                                } else {
                                    statusWithDinasMap[status] = mutableListOf(dinas)
                                }
                            }

                            // Convert the map to a list of StatusWithDinas objects
                            val statusWithDinasList = statusWithDinasMap.map { entry ->
                                historyDinas(entry.key, entry.value)
                            }

                            // Populate ExpandableListView with data
                            val expandableListView =
                                view?.findViewById<ExpandableListView>(R.id.expandableListView)
                            val adapter = DinasAdapter(
                                requireContext(),
                                statusWithDinasList,
                                "Pekerja"
                            )
                            expandableListView?.setAdapter(adapter)
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

    private fun fetchDataPerusahaanFromApi(namaPerusahaan: String) {
        val url = "http://192.168.1.4:8000/api/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
//        fetchRunnable = object : Runnable {
//            override fun run() {
                val call = apiService.getDataDinasPerusahaan(namaPerusahaan)
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            response.body()?.let { responseBody ->
                                try {
                                    val jsonResponse = responseBody.string()
                                    val responseData = JSONObject(jsonResponse)
                                    val dataArray = responseData.getJSONArray("data")

                                    // Initialize a map to hold status with corresponding Dinas list
                                    val statusWithDinasMap = mutableMapOf<String, MutableList<DinasItem>>()

                                    for (i in 0 until dataArray.length()) {
                                        val jsonObject = dataArray.getJSONObject(i)
                                        val status = jsonObject.getString("status")
                                        val tanggalBerangkatString = jsonObject.getString("tanggal_berangkat")
                                        val tanggalPulangString = jsonObject.getString("tanggal_pulang")

                                        val tanggalBerangkat = parseDate(tanggalBerangkatString)
                                        val tanggalPulang = parseDate(tanggalPulangString)
                                        val dinas = DinasItem(
                                            jsonObject.getInt("id"),
                                            jsonObject.getString("nama_pekerja"),
                                            jsonObject.getString("nama_perusahaan"),
                                            jsonObject.getString("tujuan"),
                                            tanggalBerangkat,
                                            tanggalPulang,
                                            jsonObject.getString("kegiatan"),
                                            jsonObject.getString("bukti"),
                                            jsonObject.getString("status")
                                        )

                                        // Add dinas to corresponding status list
                                        if (statusWithDinasMap.containsKey(status)) {
                                            statusWithDinasMap[status]?.add(dinas)
                                        } else {
                                            statusWithDinasMap[status] = mutableListOf(dinas)
                                        }
                                    }

                                    // Convert the map to a list of StatusWithDinas objects
                                    val statusWithDinasList = statusWithDinasMap.map { entry ->
                                        historyDinas(entry.key, entry.value)
                                    }

                                    // Populate ExpandableListView with data
                                    val expandableListView = view?.findViewById<ExpandableListView>(R.id.expandableListView)
                                    val adapter = DinasAdapter(
                                        requireContext(),
                                        statusWithDinasList,
                                        "Admin"
                                    )
                                    expandableListView?.setAdapter(adapter)
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
//
//                // Schedule the next polling after the interval
//                handler.postDelayed(this, pollingInterval)
//            }
//        }
//        // Start the initial polling
//        fetchRunnable?.let {
//            handler.post(it)
//        }
    }
    fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }
    private fun getBundle() {
        val arguments = arguments
        if (arguments != null) {
            perusahaan = arguments.getParcelable("perusahaan")
            role = arguments.getString("role")
            if(role.toString() == "Admin"){
                admin = arguments.getParcelable("user")
            }else{
                pekerja = arguments.getParcelable("user")
//                perusahaan?.let { pekerja?.let { it1 -> fetchDataFromApiPekerja(it.nama, it1.nama) } }
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}
