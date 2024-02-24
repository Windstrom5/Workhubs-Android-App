package com.example.tugasakhir

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.tugasakhir.databinding.ActivityLoginBinding
import com.example.tugasakhir.databinding.ActivityRegisterBinding
import com.example.tugasakhir.model.Perusahaan
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {
    private lateinit var location : Button
    private lateinit var TINamaPerusahaan:TextInputLayout
    private lateinit var edNamaPerusahaan:EditText
    private lateinit var perusahaan: Perusahaan
    private lateinit var bundle: Bundle
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        location = binding.selectLocationButton
        getBundle()
        TINamaPerusahaan = binding.textInputPerusahaan
        edNamaPerusahaan = binding.editTextPerusahaan
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
        bundle = intent?.getBundleExtra("data")!!
        bundle.let {
            val namaPerusahaan = it.getString("namaPerusahaan")
            val latitude = it.getDouble("latitude") // Serialize the user object to a Bundle
            val longitude = it.getDouble("longitude")
            val address = it.getString("address")
            if (namaPerusahaan!= null &&latitude != null && longitude != null && address != null){
                edNamaPerusahaan.setText(namaPerusahaan )
            }
        }
    }
}