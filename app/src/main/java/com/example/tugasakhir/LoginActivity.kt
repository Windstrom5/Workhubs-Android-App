package com.example.tugasakhir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.PopupWindow
import androidx.core.content.res.ResourcesCompat
import br.com.simplepass.loading_button_lib.customViews.CircularProgressEditText
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.tugasakhir.databinding.ActivityLoginBinding
import com.example.tugasakhir.model.Perusahaan
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class LoginActivity : AppCompatActivity() {
    private lateinit var textInputPerusahaan: TextInputLayout
    private lateinit var editTextPerusahaan: EditText
    private lateinit var textInputEmail: TextInputLayout
    private lateinit var editTextEmail: EditText
    private lateinit var textInputPassword: TextInputLayout
    private lateinit var editTextPassword: EditText
    private lateinit var popupWindow: PopupWindow
    private lateinit var binding: ActivityLoginBinding
    private lateinit var login : CircularProgressEditText
    private var perusahaanList: List<Perusahaan> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        textInputPerusahaan = binding.textInputperusahaan
        editTextPerusahaan = binding.editTextperusahaan
        textInputEmail = binding.textInputEmail
        editTextEmail = binding.editTextEmail
        textInputPassword = binding.textInputPassword
        editTextPassword = binding.editTextPassword
        login = binding.cirLoginButton
        login.isEnabled = false
        fetchPerusahaanData()
        editTextPerusahaan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                showSuggestionsPopup(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {
                updateLoginButtonState()
            }
        })
        editTextEmail.addTextChangedListener(textWatcher)
        editTextPassword.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            showSuggestionsPopup(s.toString())
        }
        override fun afterTextChanged(s: Editable?) {
            updateLoginButtonState()
        }
    }

    private fun updateLoginButtonState() {
        val isPerusahaanFilled = editTextPerusahaan.text.isNotBlank()
        val isEmailFilled = editTextEmail.text.isNotBlank()
        val isPasswordFilled = editTextPassword.text.isNotBlank()

        val isAllFieldsFilled = isPerusahaanFilled && isEmailFilled && isPasswordFilled
        login.isEnabled = isAllFieldsFilled
    }

    private fun showSuggestionsPopup(query: String) {
        if (query.isEmpty()) {
            popupWindow.dismiss()
            return
        }
        val suggestions = perusahaanList.filter { perusahaan ->
            perusahaan.nama.toLowerCase().contains(query.toLowerCase())
        }
        if (suggestions.isNotEmpty()) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                suggestions.map { it.nama }
            )

            val listView = ListView(this)
            listView.adapter = adapter

            popupWindow = PopupWindow(listView, editTextPerusahaan.width, ViewGroup.LayoutParams.WRAP_CONTENT)
            popupWindow.isFocusable = true
            popupWindow.showAsDropDown(editTextPerusahaan)

            listView.setOnItemClickListener { _, _, position, _ ->
                val selectedPerusahaan = suggestions[position]
                editTextPerusahaan.setText(selectedPerusahaan.nama)
                popupWindow.dismiss()
            }
        } else {
            // If there are no suggestions, dismiss the PopupWindow
            popupWindow.dismiss()
        }
    }

    private fun fetchPerusahaanData() {
        val url = "YOUR_BASE_URL/GetPerusahaan"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Handle the response
                perusahaanList = parseResponse(response)
                // Now you have the list of Perusahaan objects, you can use it as needed
            },
            { error ->
                // Handle error
                MotionToast.createToast(this, "Error",
                    "Error fetching data",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this,R.font.ralewaybold))
            })

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(this).add(request)
    }

    private fun parseResponse(response: JSONObject): List<Perusahaan> {
        val perusahaanList = mutableListOf<Perusahaan>()

        try {
            // Assuming your API returns a JSON array with objects representing Perusahaan
            val jsonArray = response.getJSONArray("your_array_key")

            for (i in 0 until jsonArray.length()) {
                val perusahaanObject = jsonArray.getJSONObject(i)
                val latitude = perusahaanObject.getString("latitude")
                val nama = perusahaanObject.getString("nama")
                val longtitude = perusahaanObject.getString("longtitude")
                val batas_aktif = perusahaanObject.getString("batas_aktif")
                val perusahaan = Perusahaan(nama,latitude,longtitude,batas_aktif)
                perusahaanList.add(perusahaan)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return perusahaanList
    }

}