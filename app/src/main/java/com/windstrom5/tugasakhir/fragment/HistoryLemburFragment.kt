package com.windstrom5.tugasakhir.fragment

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ExpandableListView
import androidx.compose.ui.semantics.Role
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.adapter.IzinAdapter
import com.windstrom5.tugasakhir.adapter.LemburAdapter
import com.windstrom5.tugasakhir.connection.ApiService
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.IzinItem
import com.windstrom5.tugasakhir.model.LemburItem
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import com.windstrom5.tugasakhir.model.historyIzin
import com.windstrom5.tugasakhir.model.historyLembur
import okhttp3.ResponseBody
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

class HistoryLemburFragment : Fragment(){
    private lateinit var expandableListView: ExpandableListView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: LemburAdapter
    private var perusahaan : Perusahaan? = null
    private var admin : Admin? = null
    private var pekerja : Pekerja? = null
    private var role : String? = null
    private var fetchRunnable: Runnable? = null
    private val handler = Handler()
    private val pollingInterval = 2000L
    private val filteredList = mutableListOf<historyLembur>() // For storing filtered data
    private lateinit var searchEditText: EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_lembur, container, false)
        getBundle()
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        adapter = perusahaan?.let { LemburAdapter(it,requireContext(), filteredList, role ?: "") }!!
        expandableListView.setAdapter(adapter)
        if(admin != null){
            perusahaan?.let { fetchDataPerusahaanFromApi(it.nama) }
            swipeRefreshLayout.setOnRefreshListener {
                perusahaan?.let { fetchDataPerusahaanFromApi(it.nama) }
            }
        }else{
            perusahaan?.let { fetchDataPerusahaanFromApi(it.nama) }
            swipeRefreshLayout.setOnRefreshListener {
                perusahaan?.let { pekerja?.let { it1 -> fetchDataPekerjaFromApi(it.nama, it1.nama) } }
            }
        }
        searchEditText = view.findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                // Filter the data in the adapter based on the query
                adapter.filterData(query)
            }
        })
        return view
    }
    private fun fetchDataPekerjaFromApi(namaPerusahaan: String,nama_pekerja: String) {
        val url = "http://192.168.1.6:8000/api/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getDataLemburPekerja(namaPerusahaan,nama_pekerja)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        try {
                            val jsonResponse = responseBody.string()
                            val responseData = JSONObject(jsonResponse)
                            val dataArray = responseData.getJSONArray("data")
                            val statusWithLemburMap = mutableMapOf<String, MutableList<LemburItem>>()
                            for (i in 0 until dataArray.length()) {
                                val jsonObject = dataArray.getJSONObject(i)
                                val status = jsonObject.getString("status")
                                val tanggal = jsonObject.getString("tanggal")
                                val tanggalDate = parseDate(tanggal)
                                val lembur = LemburItem(
                                    jsonObject.getInt("id"),
                                    jsonObject.getString("nama_pekerja"),
                                    jsonObject.getString("nama_perusahaan"),
                                    Date(jsonObject.getLong("tanggal")),
                                    Time.valueOf(jsonObject.getString("waktu_masuk")),
                                    Time.valueOf(jsonObject.getString("waktu_pulang")),
                                    jsonObject.getString("pekerjaan"),
                                    jsonObject.getString("bukti"),
                                    jsonObject.getString("status")
                                )
                                if (statusWithLemburMap.containsKey(status)) {
                                    statusWithLemburMap[status]?.add(lembur)
                                } else {
                                    statusWithLemburMap[status] = mutableListOf(lembur)
                                }
                            }

                            val statusWithLemburList = statusWithLemburMap.map { entry ->
                                historyLembur(entry.key, entry.value)
                            }

                            // Populate ExpandableListView with data
                            val expandableListView =
                                view?.findViewById<ExpandableListView>(R.id.expandableListView)
                            val adapter = perusahaan?.let {
                                LemburAdapter(
                                    it,
                                    requireContext(),
                                    statusWithLemburList,
                                    "Pekerja"
                                )
                            }
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
        val url = "http://192.168.1.6:8000/api/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
//        fetchRunnable = object : Runnable {
//            override fun run() {
        val call = apiService.getDataLemburPerusahaan(namaPerusahaan)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        try {
                            val jsonResponse = responseBody.string()
                            val responseData = JSONObject(jsonResponse)
                            val dataArray = responseData.getJSONArray("data")
                            val statusWithLemburMap = mutableMapOf<String, MutableList<LemburItem>>()
                            for (i in 0 until dataArray.length()) {
                                val jsonObject = dataArray.getJSONObject(i)
                                val status = jsonObject.getString("status")
                                val tanggal = jsonObject.getString("tanggal")
                                val tanggalDate = parseDate(tanggal)
                                val lembur = LemburItem(
                                    jsonObject.getInt("id"),
                                    jsonObject.getString("nama_pekerja"),
                                    jsonObject.getString("nama_perusahaan"),
                                    tanggalDate,
                                    Time.valueOf(jsonObject.getString("waktu_masuk")),
                                    Time.valueOf(jsonObject.getString("waktu_pulang")),
                                    jsonObject.getString("pekerjaan"),
                                    jsonObject.getString("bukti"),
                                    jsonObject.getString("status")
                                )
                                if (statusWithLemburMap.containsKey(status)) {
                                    statusWithLemburMap[status]?.add(lembur)
                                } else {
                                    statusWithLemburMap[status] = mutableListOf(lembur)
                                }
                            }
                            val statusWithLemburList = statusWithLemburMap.map { entry ->
                                historyLembur(entry.key, entry.value)
                            }

                            // Populate ExpandableListView with data
                            val expandableListView =
                                view?.findViewById<ExpandableListView>(R.id.expandableListView)
                            val adapter = perusahaan?.let {
                                LemburAdapter(
                                    it,
                                    requireContext(),
                                    statusWithLemburList,
                                    "Pekerja"
                                )
                            }
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
    private fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }
    private fun getBundle() {
        val arguments = arguments
        if (arguments != null) {
            perusahaan = arguments.getParcelable("perusahaan")
            role = arguments.getString("role")
            Log.d("role",role.toString())
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