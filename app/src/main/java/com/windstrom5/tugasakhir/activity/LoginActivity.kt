package com.windstrom5.tugasakhir.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.windstrom5.tugasakhir.connection.SharedPreferencesManager
import com.windstrom5.tugasakhir.databinding.ActivityLoginBinding
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.windstrom5.tugasakhir.R
import org.json.JSONException
import org.json.JSONObject
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    private lateinit var textInputPerusahaan: TextInputLayout
    private lateinit var editTextPerusahaan: AutoCompleteTextView
    private lateinit var textInputEmail: TextInputLayout
    private lateinit var editTextEmail: EditText
    private lateinit var textInputPassword: TextInputLayout
    private lateinit var editTextPassword: EditText
    private lateinit var popupWindow: PopupWindow
    private lateinit var binding: ActivityLoginBinding
    private lateinit var login : Button
    private lateinit var acperusahaan : AutoCompleteTextView
    private val selectedPerusahaanId = null
    private lateinit var register : TextView
    private var perusahaanList: List<Perusahaan> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        textInputPerusahaan = binding.textInputperusahaan
        editTextPerusahaan = binding.ACperusahaan
        textInputEmail = binding.textInputEmail
        editTextEmail = binding.editTextEmail
        textInputPassword = binding.textInputPassword
        editTextPassword = binding.editTextPassword
        login = binding.cirLoginButton
        register = binding.createPerusahan
        login.isEnabled = false
        fetchDataFromApi()
        editTextEmail.addTextChangedListener(textWatcher)
        editTextPassword.addTextChangedListener(textWatcher)
        editTextPerusahaan.addTextChangedListener(textWatcher)
        login.setOnClickListener{
            Log.d("Ambatukam5","Ambatukam5")
            login(textInputPerusahaan.editText?.text.toString(),
                textInputEmail.editText?.text.toString(),
                textInputPassword.editText?.text.toString())
        }
        register.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchDataFromApi() {
        val url = "https://3fad-125-163-245-254.ngrok-free.app/api/GetPerusahaan"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val perusahaanArray = response.getJSONArray("perusahaan")
                val newPerusahaanList = mutableListOf<Perusahaan>()
                for (i in 0 until perusahaanArray.length()) {
                    val perusahaanObject = perusahaanArray.getJSONObject(i)
                    val id = perusahaanObject.getInt("id")
                    val nama = perusahaanObject.getString("nama")
                    val latitude = perusahaanObject.getDouble("latitude")
                    val longitude = perusahaanObject.getDouble("longitude")
                    val jam_masukStr = perusahaanObject.getString("jam_masuk")
                    val jam_keluarStr = perusahaanObject.getString("jam_keluar")
                    val jam_masuk = convertStringToTime(jam_masukStr)
                    val jam_keluar = convertStringToTime(jam_keluarStr)
                    val batasAktif = perusahaanObject.getString("batas_aktif")
                    val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val javaUtilDate = dateParser.parse(batasAktif)

                    // Convert java.util.Date to java.sql.Date
                    val sqlDate = java.sql.Date(javaUtilDate.time)
                    val logo = perusahaanObject.getString("logo")
                    val secretKey = perusahaanObject.getString("secret_key")
                    val perusahaan = Perusahaan(id,nama, latitude, longitude, jam_masuk,jam_keluar,sqlDate, logo, secretKey)
                    newPerusahaanList.add(perusahaan)
                }
                perusahaanList = newPerusahaanList
                setUpAutoCompleteTextView(perusahaanList)
            },
            { error ->
                error.printStackTrace()
            })

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
    @SuppressLint("SimpleDateFormat")
    fun convertStringToTime(timeStr: String): Time {
        val sdf = SimpleDateFormat("HH:mm:ss")
        val date: Date = sdf.parse(timeStr)
        return Time(date.time)
    }
    private fun setUpAutoCompleteTextView(perusahaanList: List<Perusahaan>) {
        val autoCompleteTextView: AutoCompleteTextView = findViewById(R.id.ACperusahaan)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            perusahaanList.map { it.nama }
        )
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedPerusahaan = perusahaanList[position]
        }
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used
            }

            override fun afterTextChanged(s: Editable?) {
                // Handle text changes
                val searchText = s.toString().trim()

                // Filter the list based on the current search text
                val filteredList = perusahaanList.filter { it.nama.contains(searchText, true) }

                // Update the adapter with the filtered list
                val filteredAdapter = ArrayAdapter(
                    this@LoginActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    filteredList.map { it.nama }
                )
                autoCompleteTextView.setAdapter(filteredAdapter)
            }
        })
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

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
    private fun login(namaPerusahaan: String, email: String, password: String) {
        val url = "https://3fad-125-163-245-254.ngrok-free.app/api/login"
        val sharedPreferencesManager = SharedPreferencesManager(this)
        val matchingPerusahaan = perusahaanList.find { it.nama == namaPerusahaan }
        if (matchingPerusahaan != null) {
            Log.d("Ambatukam1",namaPerusahaan)
            Log.d("Ambatukam1",email)
            Log.d("Ambatukam1",password)
            val jsonParams = JSONObject()
            jsonParams.put("email", email)
            jsonParams.put("password", password)
            jsonParams.put("perusahaan", namaPerusahaan)
            val request = JsonObjectRequest(
                Request.Method.POST, url, jsonParams,
                { response ->
                    try {
                        val token = response.getString("token")
                        val user = response.getJSONObject("user")
                        val role = response.getString("Role")
                        sharedPreferencesManager.clearUserData()
                        if (role == "Admin") {
                            val admin = Admin(
                                user.getInt("id"),
                                user.getInt("id_perusahaan"),
                                user.getString("email"),
                                user.getString("password"),
                                user.getString("nama"),
                                // Parse tanggal_lahir accordingly
                                parseDate(user.getString("tanggal_lahir")),
                                user.getString("profile")
                            )
                            sharedPreferencesManager.saveAdmin(admin)
                            sharedPreferencesManager.savePerusahaan(matchingPerusahaan)
                            val intent = Intent(this, AdminActivity::class.java)
                            val userBundle = Bundle()
                            userBundle.putParcelable("user", admin)
                            userBundle.putParcelable("perusahaan", matchingPerusahaan)
                            intent.putExtra("data", userBundle)
                            startActivity(intent)
                        } else {
                            val pekerja = Pekerja(
                                user.getInt("id"),
                                user.getInt("id_perusahaan"),
                                user.getString("email"),
                                user.getString("password"),
                                user.getString("nama"),
                                parseDate(user.getString("tanggal_lahir")),
                                user.getString("profile")
                            )
                            sharedPreferencesManager.savePekerja(pekerja)
                            sharedPreferencesManager.savePerusahaan(matchingPerusahaan)
                            val intent = Intent(this, UserActivity::class.java)
                            val userBundle = Bundle()
                            userBundle.putParcelable("user", pekerja)
                            userBundle.putParcelable("perusahaan", matchingPerusahaan)
                            intent.putExtra("data", userBundle)
                            startActivity(intent)
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.d("Ambatukam","Ambatukam")
                    }
                },
                { error ->
                    MotionToast.createToast(this@LoginActivity, "Error",
                        "Email atau Password Anda Salah",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this@LoginActivity, R.font.ralewaybold))
                }
            )
            Volley.newRequestQueue(this).add(request)
        }
    }
    private fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }
}