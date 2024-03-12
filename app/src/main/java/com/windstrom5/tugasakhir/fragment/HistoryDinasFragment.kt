package com.windstrom5.tugasakhir.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.adapter.DinasExpandablePekerjaListAdapter
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Dinas
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import org.json.JSONObject
import java.util.Date

class HistoryDinasFragment : Fragment() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var dinasListAdapter: DinasExpandablePekerjaListAdapter
    private var perusahaan : Perusahaan? = null
    private var admin : Admin? = null
    private var pekerja : Pekerja? = null
    private var role : String? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expandableListView = view.findViewById(R.id.expandableListView)
        getBundle()
    }
    private fun fetchDataFromApiPekerja(companyName: String,pekerjaName: String) {
        val queue = Volley.newRequestQueue(requireContext())
        val url = "https://9ca5-125-163-245-254.ngrok-free.app/api//getDataLemburPekerja/$companyName/$pekerjaName"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                // Parse the JSON response and update the adapter
                updateAdapterWithResponse(response)
            },
            { error ->
                // Handle error
                error.printStackTrace()
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }
    private fun fetchDataFromApiAdmin(companyName: String) {
        val queue = Volley.newRequestQueue(requireContext())
        val url = "https://9ca5-125-163-245-254.ngrok-free.app/api/getDataLemburPerusahaan/$companyName"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                // Parse the JSON response and update the adapter
                updateAdapterWithResponse(response)
            },
            { error ->
                // Handle error
                error.printStackTrace()
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }
    private fun getBundle() {
        val arguments = arguments
        if (arguments != null) {
            perusahaan = arguments.getParcelable("perusahaan")
            role = arguments.getString("role")
            if(role == "Admin"){
                perusahaan?.let { fetchDataFromApiAdmin(it.nama) }
                admin = arguments.getParcelable("user")
            }else{
                pekerja = arguments.getParcelable("user")
                perusahaan?.let { pekerja?.let { it1 -> fetchDataFromApiPekerja(it.nama, it1.nama) } }
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
    private fun updateAdapterWithResponse(response: String) {
        val dinasList: MutableList<Dinas> = mutableListOf()

        try {
            val jsonObject = JSONObject(response)
            val dataArray = jsonObject.getJSONArray("data")

            for (i in 0 until dataArray.length()) {
                val dinasObject = dataArray.getJSONObject(i)
                val dinas = Dinas(
                    dinasObject.optInt("id"),
                    dinasObject.optInt("id_pekerja"),
                    dinasObject.optInt("id_perusahaan"),
                    dinasObject.optString("tujuan"),
                    Date(dinasObject.optLong("tanggal_berangkat")),
                    Date(dinasObject.optLong("tanggal_pulang")),
                    dinasObject.optString("kegiatan"),
                    dinasObject.optString("bukti"),
                    dinasObject.optString("status")
                )
                dinasList.add(dinas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Update the adapter with the new data
        dinasListAdapter = DinasExpandablePekerjaListAdapter(requireContext(), dinasList)
        expandableListView.setAdapter(dinasListAdapter)
        dinasListAdapter.notifyDataSetChanged()
    }
}
