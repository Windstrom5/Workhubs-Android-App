package com.windstrom5.tugasakhir.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.adapter.AbsenAdapter
import com.windstrom5.tugasakhir.model.Absen
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import org.json.JSONException
import java.sql.Date

class HistoryAbsenFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var absenAdapter: AbsenAdapter
    private var perusahaan: Perusahaan? = null
    private var pekerja: Pekerja? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_absen, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch data from API
        fetchDataFromApi()

        return view
    }

    private fun fetchDataFromApi() {
        val url = "http://127.0.0.1:8000/api/"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val absenList = mutableListOf<Absen>()

                    for (i in 0 until response.length()) {
                        val absenObject = response.getJSONObject(i)
                        val absen = Absen(
                            absenObject.getInt("id"),
                            absenObject.getInt("idPekerja"),
                            absenObject.getInt("idPerusahaan"),
                            Date(absenObject.getLong("tanggal")),
                            absenObject.getString("jamMasuk"),
                            absenObject.getString("jamKeluar"),
                            absenObject.getDouble("latitude"),
                            absenObject.getDouble("longitude"),
                        )
                        absenList.add(absen)
                    }
                    setupRecyclerView(absenList)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            })

        // Add the request to the request queue
        Volley.newRequestQueue(requireContext()).add(jsonArrayRequest)
    }

    private fun setupRecyclerView(absenList: List<Absen>) {
        absenAdapter = AbsenAdapter(absenList, perusahaan!!, pekerja!!)
        recyclerView.adapter = absenAdapter
    }
}