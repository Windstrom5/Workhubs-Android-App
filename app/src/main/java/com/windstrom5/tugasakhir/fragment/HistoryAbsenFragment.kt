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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryAbsenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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
                            Date(absenObject.getLong("updatedAt"))
                        )
                        absenList.add(absen)
                    }

                    // Now you have a list of Absen objects (absenList)
                    // Pass this list to your RecyclerView adapter
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