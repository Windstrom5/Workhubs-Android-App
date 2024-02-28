package com.example.tugasakhir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.tugasakhir.databinding.ActivityLoginBinding
import com.example.tugasakhir.databinding.ActivityRegisterBinding
import com.example.tugasakhir.model.Perusahaan
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {
    private lateinit var location : Button
    private lateinit var TINamaPerusahaan:TextInputLayout
    private lateinit var tvaddress : TextView
    private lateinit var Tvlongitude:TextView
    private lateinit var Tvlatitude:TextView
    private lateinit var edNamaPerusahaan:EditText
    private lateinit var perusahaan: Perusahaan
    private var bundle: Bundle? = null
    private lateinit var information:CardView
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        location = binding.selectLocationButton
        TINamaPerusahaan = binding.textInputPerusahaan
        edNamaPerusahaan = binding.editTextPerusahaan
        tvaddress = binding.tvAddress

        Tvlongitude = binding.tvlongitude
        Tvlatitude = binding.tvLatitude
        information = binding.information
        getBundle()
        location.setOnClickListener{
            if (edNamaPerusahaan.text.isNotEmpty()) {
                val intent = Intent(this, MapActivity::class.java)
                val bundle = Bundle()
                bundle.putString("nama", edNamaPerusahaan.text.toString())
                intent.putExtra("data", bundle)
                startActivity(intent)
            } else {
                startActivity(Intent(this, MapActivity::class.java))
            }
        }
    }
    private fun getBundle() {
        bundle = intent?.getBundleExtra("data")
        if (bundle != null) {
            bundle?.let {
                val namaPerusahaan = it.getString("namaPerusahaan")
                val latitude = it.getDouble("latitude")
                val longitude = it.getDouble("longitude")
                val address = it.getString("address")
                if (namaPerusahaan != null && address != null) {
                    information.visibility= View.VISIBLE
                    tvaddress.text = address.toString()
                    Tvlatitude.text = latitude.toString()
                    Tvlongitude.text = longitude.toString()
                    edNamaPerusahaan.setText(namaPerusahaan)
                }
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}