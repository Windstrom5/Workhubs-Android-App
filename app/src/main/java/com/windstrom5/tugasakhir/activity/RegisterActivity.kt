package com.windstrom5.tugasakhir.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.connection.ApiResponse
import com.windstrom5.tugasakhir.connection.ApiService
import com.windstrom5.tugasakhir.databinding.ActivityRegisterBinding
import com.windstrom5.tugasakhir.model.Perusahaan
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.security.SecureRandom
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import retrofit2.Response as RetrofitResponse

class RegisterActivity : AppCompatActivity() {
    private lateinit var location : Button
    private lateinit var TINamaPerusahaan:TextInputLayout
    private lateinit var TIJammasuk:TextInputLayout
    private lateinit var TIJamkeluar:TextInputLayout
    private lateinit var tvaddress : TextView
    private lateinit var Tvlongitude:TextView
    private lateinit var requestQueue: RequestQueue
    private lateinit var Tvlatitude:TextView
    private lateinit var edNamaPerusahaan:EditText
    private lateinit var selectedFileName : TextView
    private var perusahaan : Perusahaan? = null
    private var bundle: Bundle? = null
    private lateinit var information:CardView
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var upload:Button
    private lateinit var createnow:Button
    private var selectedImageByteArray: ByteArray? = null
    private lateinit var selectedFile: File
    private lateinit var progressBar:ProgressBar
    private lateinit var textViewProgress: TextView
    private lateinit var textViewCustom:TextView
    private val boundary = "*****"
    private lateinit var path : String
    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 123
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestQueue = Volley.newRequestQueue(this)
        location = binding.selectLocationButton
        TINamaPerusahaan = binding.textInputPerusahaan
        TIJamkeluar = binding.textInputkeluar
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        textViewProgress  = findViewById<TextView>(R.id.textViewProgress)
        textViewCustom = findViewById<TextView>(R.id.textViewCustom)
        TIJammasuk = binding.textInputMasuk
        edNamaPerusahaan = binding.editTextPerusahaan
        tvaddress = binding.tvAddress
        selectedFileName = binding.selectedFileName
        Tvlongitude = binding.tvlongitude
        Tvlatitude = binding.tvLatitude
        information = binding.information
        upload = binding.uploadfile
        createnow = binding.cirLoginButton
        getBundle()
        location.setOnClickListener{
            if (edNamaPerusahaan.text.isNotEmpty()) {
                val intent = Intent(this, MapActivity::class.java)
                val bundle = Bundle()
                bundle.putString("namaPerusahaan", TINamaPerusahaan.editText?.text.toString())
                bundle.putString("openhour", TIJammasuk.editText?.text.toString())
                bundle.putString("closehour", TIJamkeluar.editText?.text.toString())
                intent.putExtra("data", bundle)
                startActivity(intent)
            } else {
                startActivity(Intent(this, MapActivity::class.java))
            }
        }
        TIJammasuk.setEndIconOnClickListener{
            showTimePicker(TIJammasuk)
        }
        TIJamkeluar.setEndIconOnClickListener{
            showTimePicker(TIJamkeluar)
        }
        upload.setOnClickListener{
            pickImageFromGallery()
        }
        createnow.setOnClickListener {
            val secretKey = generateRandomString()
            makeApiRequest(secretKey)
//            val calendar = Calendar.getInstance()
//            calendar.add(Calendar.YEAR, 1)
//            val futureDate = calendar.time
//            val sqlDate = java.sql.Date(futureDate.time)
//            perusahaan = Perusahaan(TINamaPerusahaan.editText?.text.toString(),
//                Tvlatitude.text.toString().toDouble(),
//                Tvlongitude.text.toString().toDouble(),
//                stringToSqlTime(TIJammasuk.editText?.text.toString()),
//                stringToSqlTime(TIJamkeluar.editText?.text.toString()),
//                sqlDate,
//                secretKey,
//                path
//            )
//            val intent = Intent(this,RegisterAdminActivity::class.java)
//            val Bundle = Bundle()
//            Bundle.putParcelable("perusahaan", perusahaan)
//            intent.putExtra("data", Bundle)
//            startActivity(intent)
        }
    }
    private fun savePerusahaan(perusahaan2: Perusahaan){
        perusahaan = perusahaan2
    }
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }
    private fun generateRandomString(): String {
        // Fetch existing secret keys from the server
        val existingSecretKeys = getSecretKeysFromApi()

        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+"
        val random = SecureRandom()

        while (true) {
            val randomString = StringBuilder()
            for (i in 0 until 20) {
                val randomChar = charset[random.nextInt(charset.length)]
                randomString.append(randomChar)
            }

            // Check if the generated key already exists
            if (!existingSecretKeys.contains(randomString.toString())) {
                return randomString.toString()
            }
        }
    }
    private fun getSecretKeysFromApi(): List<String> {
        val apiUrl = "https://2349-36-80-222-40.ngrok-free.app/api/GetPerusahaan"

        val secretKeysList = mutableListOf<String>()

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, apiUrl, null,
            { response ->
                // Parse the JSON array and extract secret keys
                for (i in 0 until response.length()) {
                    val secretKey = response.getString(i)
                    secretKeysList.add(secretKey)
                }
            },
            { error ->
                // Handle error cases
            })

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest)

        return secretKeysList
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                // Get the real path from the URI
                val realPath = getRealPathFromUri(imageUri)
                if (realPath != null) {
                    selectedFileName.text = File(realPath).name
                    selectedFile = File(realPath)
                } else {
                    selectedFileName.text = "Failed to get real path"
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

    private fun showTimePicker(textInputLayout: TextInputLayout) {
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

            textInputLayout.editText?.setText(formattedTime)
        }

        timePicker.show(supportFragmentManager, "timePicker")
    }
    fun stringToSqlTime(timeString: String): Time {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = dateFormat.parse(timeString)
        return Time(date.time)
    }

    private fun makeApiRequest(secretKey: String) {
        val url = "https://2349-36-80-222-40.ngrok-free.app/api/"

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // Convert values to RequestBody
        val nama = createPartFromString(TINamaPerusahaan.editText?.text.toString())
        val latitude = createPartFromString(Tvlatitude.text.toString().toDouble().toString())
        val longitude = createPartFromString(Tvlongitude.text.toString().toDouble().toString())
        val jamMasuk = createPartFromString(stringToSqlTime(TIJammasuk.editText?.text.toString()).toString())
        val jamKeluar = createPartFromString(stringToSqlTime(TIJamkeluar.editText?.text.toString()).toString())
        val batasAktif = createPartFromString(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time))
        val secretKeyPart = createPartFromString(secretKey)

        val logoFile = selectedFile
        val requestFile = RequestBody.create(MediaType.parse("image/*"), logoFile)
        val logoPart = MultipartBody.Part.createFormData("logo", logoFile.name, requestFile)

        // Make the API call
        val call = apiService.uploadPerusahaan(nama, latitude, longitude, jamMasuk, jamKeluar, batasAktif, secretKeyPart, logoPart)

        // Execute the call asynchronously
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: RetrofitResponse<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Log.d("ApiResponse", "Status: ${apiResponse?.status}, Message: ${apiResponse?.message}")
                    Log.d("path", " ${apiResponse?.filepath}")
                    // Assuming path is a global variable
                    val path = apiResponse?.filepath ?: ""

                    // Continue with other actions after API response
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.YEAR, 1)
                    val futureDate = calendar.time
                    val sqlDate = java.sql.Date(futureDate.time)

                    perusahaan = Perusahaan(
                        TINamaPerusahaan.editText?.text.toString(),
                        Tvlatitude.text.toString().toDouble(),
                        Tvlongitude.text.toString().toDouble(),
                        stringToSqlTime(TIJammasuk.editText?.text.toString()),
                        stringToSqlTime(TIJamkeluar.editText?.text.toString()),
                        sqlDate,
                        secretKey,
                        path
                    )

                    val intent = Intent(this@RegisterActivity, RegisterAdminActivity::class.java)
                    val bundle = Bundle()
                    bundle.putParcelable("perusahaan", perusahaan)
                    intent.putExtra("data", bundle)
                    startActivity(intent)
                } else {
                    Log.e("ApiResponse", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("ApiResponse", "Request failed: ${t.message}")
            }
        })
    }

    private fun createPartFromString(value: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), value)
    }

//    private fun makeApiRequest(secretKey: String) {
//        val url = "https://1469-36-73-58-191.ngrok-free.app/api/DaftarPerusahaan"
//        val jsonObject = JSONObject()
//        try {
//            jsonObject.put("nama", edNamaPerusahaan.text.toString())
//            jsonObject.put("latitude", Tvlatitude.text.toString().toDouble())
//            jsonObject.put("longitude", Tvlongitude.text.toString().toDouble())
//            val jam_masuk = stringToSqlTime(TIJammasuk.editText?.text.toString())
//            val jam_keluar = stringToSqlTime(TIJamkeluar.editText?.text.toString())
//            jsonObject.put("jam_masuk", jam_masuk)
//            jsonObject.put("jam_keluar", jam_keluar)
//            val currentDate = Calendar.getInstance()
//            currentDate.add(Calendar.YEAR, 1)
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val formattedDate = dateFormat.format(currentDate.time)
//            jsonObject.put("batas_aktif", formattedDate)
//            jsonObject.put("secret_key", secretKey)
//
//            val volleyMultipartRequest = object : VolleyMultipartRequest(
//                Method.POST, url,
//                Response.Listener { response ->
//                    createnow.stopAnimation()
//                    MotionToast.createToast(
//                        this,
//                        "Success",
//                        "Pendaftaran Perusahaan Sukses",
//                        MotionToastStyle.SUCCESS,
//                        MotionToast.GRAVITY_BOTTOM,
//                        MotionToast.LONG_DURATION,
//                        ResourcesCompat.getFont(
//                            this,
//                            www.sanju.motiontoast.R.font.helvetica_regular
//                        )
//                    )
//                },
//                Response.ErrorListener { error ->
//                    createnow.stopAnimation()
//                    MotionToast.createToast(
//                        this,
//                        "Error",
//                        " ${error.message}",
//                        MotionToastStyle.ERROR,
//                        MotionToast.GRAVITY_BOTTOM,
//                        MotionToast.LONG_DURATION,
//                        ResourcesCompat.getFont(
//                            this,
//                            www.sanju.motiontoast.R.font.helvetica_regular
//                        )
//                    )
//                }
//            ) {
//                override fun getParams(): Map<String, String> {
//                    val params = HashMap<String, String>()
//                    params["nama"] = edNamaPerusahaan.text.toString()
//                    params["latitude"] = Tvlatitude.text.toString()
//                    params["longitude"] = Tvlongitude.text.toString()
//                    params["jam_masuk"] = TIJammasuk.editText?.text.toString()
//                    params["jam_keluar"] = TIJamkeluar.editText?.text.toString()
//                    params["batas_aktif"] = formattedDate
//                    params["secret_key"] = secretKey
//                    return params
//                }
//
//                override fun getByteData(): Map<String, DataPart>? {
//                    val params = HashMap<String, DataPart>()
//                    params["logo"] = DataPart(
//                        selectedFile.name,
//                        AppHelper.getFileDataFromDrawable(selectedFile),
//                        "image/*"
//                    )
//                    return params
//                }
//            }
//
//            // Add the request to the RequestQueue
//            requestQueue.add(volleyMultipartRequest)
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//    }

    private fun getBundle() {
        bundle = intent?.getBundleExtra("data")
        if (bundle != null) {
            bundle?.let {
                val namaPerusahaan = it.getString("namaPerusahaan") ?: ""
                val openHours = it.getString("openhour") ?: ""
                val closeHours = it.getString("closehour") ?: ""
                val latitude = it.getDouble("latitude")
                val longitude = it.getDouble("longitude")
                val address = it.getString("address")
                if (address != null) {
                    information.visibility= View.VISIBLE
                    tvaddress.text = address.toString()
                    Tvlatitude.text = latitude.toString()
                    Tvlongitude.text = longitude.toString()
                    edNamaPerusahaan.setText(namaPerusahaan)
                    TIJamkeluar.editText?.setText(closeHours)
                    TIJammasuk.editText?.setText(openHours)
                }
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}