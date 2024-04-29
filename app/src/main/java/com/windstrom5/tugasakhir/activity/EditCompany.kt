package com.windstrom5.tugasakhir.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.databinding.ActivityEditCompanyBinding
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan

class EditCompany : AppCompatActivity() {
    private lateinit var binding: ActivityEditCompanyBinding
    private lateinit var editNama : ImageView
    private lateinit var editJamMasuk: ImageView
    private lateinit var editJamKeluar: ImageView
    private lateinit var editLocation : ImageView
    private var perusahaan : Perusahaan? = null
    private var admin : Admin? = null
    private var pekerja : Pekerja? = null
    private var bundle: Bundle? = null
    private lateinit var enteredText: String
    private lateinit var role:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getBundle()
        editJamMasuk = binding.editJamMasuk
        editNama = binding.editNamaPerusahaan
        editLocation = binding.editLocation
        editJamKeluar = binding.editJamKeluar
        editNama.setOnClickListener{
            showNamaTextDialog()
        }
    }
    private fun showNamaTextDialog() {
        val textInputLayout = TextInputLayout(this@EditCompany)
        val editText = EditText(this@EditCompany)
        textInputLayout.hint = "Nama Perusahaan"
        editText.setText(perusahaan?.nama)
        textInputLayout.addView(editText)

        val dialogBuilder = AlertDialog.Builder(this@EditCompany)
            .setView(textInputLayout)
            .setTitle("Enter Text")
            .setPositiveButton("OK") { dialog, which ->
                // Handle the text input when the user clicks OK
                enteredText = editText.text.toString()
                // Do something with the entered text
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // Handle the cancellation if needed
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }
    private fun getBundle() {
        bundle = intent?.getBundleExtra("data")
        if (bundle != null) {
            bundle?.let {
                perusahaan = it.getParcelable("perusahaan")
                role = it.getString("role").toString()
                if(role == "Admin"){
                    admin = it.getParcelable("user")
                }else{
                    pekerja = it.getParcelable("user")
                }
                val imageUrl =
                    "http://192.168.1.4:8000/storage/${perusahaan?.logo}" // Replace with your Laravel image URL
                val profileImageView = binding.logo
                val text = binding.companyNameTextView
                text.setText(perusahaan?.nama)
                Glide.with(this)
                    .load(imageUrl)
                    .into(profileImageView)
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}