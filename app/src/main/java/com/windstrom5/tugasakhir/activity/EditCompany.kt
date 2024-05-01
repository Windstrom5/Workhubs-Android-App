package com.windstrom5.tugasakhir.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.databinding.ActivityEditCompanyBinding
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditCompany : AppCompatActivity() {
    private lateinit var binding: ActivityEditCompanyBinding
    private lateinit var editNama : ImageView
    private lateinit var editJamMasuk: ImageView
    private lateinit var editJamKeluar: ImageView
    private lateinit var editLocation : ImageView
    private var perusahaan : Perusahaan? = null
    private var admin : Admin? = null
    private var pekerja : Pekerja? = null
    private var selectedFile: File?= null
    private var bundle: Bundle? = null
    private lateinit var enteredText: String
    private lateinit var role:String
    private lateinit var textNama : TextView
    private lateinit var textJamMasuk : TextView
    private lateinit var textJamKeluar : TextView
    private lateinit var changeProfile : ImageView
    private lateinit var profile:CircleImageView
    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 123
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        textNama = binding.companyNameTextView
        textJamKeluar = binding.JamKeluarText
        textJamMasuk = binding.jamMasukText
        changeProfile = binding.selectImage
        profile = binding.logo
        getBundle()
        editJamMasuk = binding.editJamMasuk
        editNama = binding.editNamaPerusahaan
        editLocation = binding.editLocation
        editJamKeluar = binding.editJamKeluar
        editNama.setOnClickListener{
            showNamaTextDialog()
        }
        changeProfile.setOnClickListener{
            pickImageFromGallery()
        }
        editLocation.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            val bundle = Bundle()
            bundle.putString("namaPerusahaan", textNama.toString())
            bundle.putString("openhour", textJamMasuk.toString())
            bundle.putString("closehour", textJamKeluar.toString())
            bundle.putString("category","edit")
            intent.putExtra("data", bundle)
            startActivity(intent)
        }
        editJamKeluar.setOnClickListener{
            showTimePicker(textJamKeluar)
        }
        editJamMasuk.setOnClickListener{
            showTimePicker(textJamMasuk)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }
    private fun showTimePicker(textView: TextView) {
        val calendar = Calendar.getInstance()

        val timePicker = MaterialTimePicker.Builder()
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = timeFormat.format(calendar.time)

            textView.setText(formattedTime)
        }

        timePicker.show(supportFragmentManager, "timePicker")
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                // Get the real path from the URI
                val realPath = getRealPathFromUri(imageUri)
                if (realPath != null) {
                    selectedFile = File(realPath)
                    Glide.with(this)
                        .load(imageUri) // Load the image using the URI
                        .into(profile) // Set it into the ImageView
                }
            }
        }
    }
    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(columnIndex)
        }
        return null
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
                    "http://192.168.1.3:8000/storage/${perusahaan?.logo}" // Replace with your Laravel image URL
                textNama.setText(perusahaan?.nama)
                textJamMasuk.setText(perusahaan?.jam_masuk.toString())
                textJamKeluar.setText(perusahaan?.jam_keluar.toString())
                Glide.with(this)
                    .load(imageUrl)
                    .into(profile)
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}