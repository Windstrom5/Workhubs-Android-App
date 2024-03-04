package com.windstrom5.tugasakhir.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import org.json.JSONObject
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.windstrom5.tugasakhir.connection.ApiService
import com.windstrom5.tugasakhir.databinding.ActivityRegisterAdminBinding
import com.windstrom5.tugasakhir.model.Admin
import com.google.android.material.textfield.TextInputLayout
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.connection.ApiResponse
import com.windstrom5.tugasakhir.connection.SharedPreferencesManager
import com.windstrom5.tugasakhir.model.Perusahaan
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterAdminActivity : AppCompatActivity() {
    private lateinit var TINama: TextInputLayout
    private lateinit var edNama: EditText
    private lateinit var alertDialog: AlertDialog
    private lateinit var TIEmail: TextInputLayout
    private lateinit var edEmail: EditText
    private lateinit var TIPassword: TextInputLayout
    private lateinit var edPassword: EditText
    private lateinit var TITanggal: TextInputLayout
    private lateinit var edTanggal: EditText
    private lateinit var save : Button
    private var bundle: Bundle? = null
    private lateinit var selectedFile: File
    private lateinit var admin: Admin
    private lateinit var binding: ActivityRegisterAdminBinding
    private lateinit var circleImageView: CircleImageView
    private lateinit var selectedImage: ByteArray
    var selectedDateSqlFormat: String? = null
    private lateinit var selectImage: ImageView
    private val CAMERA_PERMISSION_REQUEST = 124
    private val CAMERA_CAPTURE_REQUEST = 126
    private var idPerusahaan: Int? = null
    private var tempImageFile: File? = null
    private lateinit var namaPerusahaan: String
    private var perusahaan : Perusahaan? = null
    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 1
    }
    private lateinit var requestQueue: RequestQueue
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getBundle()
        requestQueue = Volley.newRequestQueue(this)
        circleImageView = binding.circleImageView
        TINama = binding.textInputNama
        selectImage = binding.selectImage
        TIEmail = binding.textInputEmail
        TIPassword = binding.textInputPassword
        TITanggal = binding.textInputTanggal
        TITanggal.editText?.apply {
            inputType = InputType.TYPE_NULL
            isFocusable = false
            isClickable = true
        }
        selectImage.setOnClickListener{
            showImagePickerDialog()
        }
        TITanggal.setOnClickListener{
            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    // Update your TextInputEditText with the selected date
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)

                    // Format the date as needed
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    val formattedDate = dateFormat.format(selectedDate.time)
                    val dateFormatSql = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDateSqlFormat = dateFormatSql.format(selectedDate.time)
                    TITanggal.editText?.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Set date picker restrictions if needed
            datePicker.datePicker.maxDate = System.currentTimeMillis()  // Optional: Set a max date
            // datePicker.datePicker.minDate = System.currentTimeMillis() - 1000  // Optional: Set a min date

            datePicker.show()
        }
        save = binding.cirsaveButton
        save.setOnClickListener{
            getPerusahaan(namaPerusahaan)
            val url = "https://2349-36-80-222-40.ngrok-free.app/api/"
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            val id_perusahaan = RequestBody.create(MediaType.parse("text/plain"), idPerusahaan.toString())
            val emailRequestBody = RequestBody.create(MediaType.parse("text/plain"), TIEmail.editText?.text.toString())
            val passwordRequestBody = RequestBody.create(MediaType.parse("text/plain"), TIPassword.editText?.text.toString())
            val nama = RequestBody.create(MediaType.parse("text/plain"), TINama.editText?.text.toString())
            val tanggalRequestBody = RequestBody.create(MediaType.parse("text/plain"), selectedDateSqlFormat)
            val imageRequestBody = RequestBody.create(MediaType.parse("image/*"), selectedImage)
            val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", imageRequestBody)
            val call = apiService.uploadAdmin(id_perusahaan, emailRequestBody, passwordRequestBody, nama,tanggalRequestBody,
                imagePart)
            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val jsonString = responseBody.toString()

                        // Parse the JSON response to get the file path
                        val jsonObject = JSONObject(jsonString)
                        if (jsonObject.has("file_path")) {
                            val filePath = jsonObject.getString("file_path")
                            MotionToast.createToast(this@RegisterAdminActivity, "Success",
                                "Berhasil Menyimpan Data",
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(this@RegisterAdminActivity,
                                    R.font.ralewaybold
                                ))
                            val intent = Intent(this@RegisterAdminActivity, AdminActivity::class.java)
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val date = dateFormat.parse(TITanggal.editText?.text.toString())!!
                            val admin = idPerusahaan?.let { it1 ->
                                Admin(
                                    it1,
                                    TIEmail.editText?.text.toString(),
                                    TIPassword.editText?.text.toString(),
                                    TINama.editText?.text.toString(),
                                    date,
                                    filePath
                                )
                            }
                            val sharedPreferencesManager = SharedPreferencesManager(this@RegisterAdminActivity)
                            if (admin != null) {
                                sharedPreferencesManager.saveAdmin(admin)
                            }
                            val userBundle = Bundle()
                            userBundle.putParcelable("user", admin)
                            userBundle.putParcelable("perusahaan", perusahaan)
                            intent.putExtra("data", userBundle)
                            startActivity(intent)
                        } else {
                            MotionToast.createToast(this@RegisterAdminActivity, "Error",
                                "Kesalahan berpikir",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(this@RegisterAdminActivity,
                                    R.font.ralewaybold
                                ))
                        }

                    } else {
                        MotionToast.createToast(this@RegisterAdminActivity, "Error",
                            "Tidak Dapat Menyimpan Data",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this@RegisterAdminActivity, R.font.ralewaybold))
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    MotionToast.createToast(this@RegisterAdminActivity,
                        "Failure",
                        "Something went wrong",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this@RegisterAdminActivity, R.font.ralewaybold))
                }
            })
        }
    }

    private fun getPerusahaan(namaPerusahaan: String) {
        val apiUrl = "https://2349-36-80-222-40.ngrok-free.app/api/namaPerusahaan/$namaPerusahaan"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { response ->
                // Handle the JSON response
                val perusahaanObject = response.getJSONObject("perusahaan")

                // Extract relevant information from the perusahaanObject
                idPerusahaan = perusahaanObject.getInt("id")
            },
            { error ->
                // Handle error cases
                Toast.makeText(
                    applicationContext,
                    "Error getting Perusahaan: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            })

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    private fun createPartFromString(value: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), value)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }
    private fun uploadData(id_perusahaan: String) {
        val url = "https://2349-36-80-222-40.ngrok-free.app/api/DaftarAdmin/"

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // Convert values to RequestBody
        val id_Perusahaan = createPartFromString(id_perusahaan)
        val email = createPartFromString(TIEmail.editText?.text.toString())
        val password = createPartFromString(TIPassword.editText?.text.toString())
        val nama = createPartFromString(TINama.editText?.text.toString())
        val tanggal_lahir = createPartFromString(TITanggal.editText?.text.toString())
        val logoFile = selectedFile
        val requestFile = RequestBody.create(MediaType.parse("image/*"), logoFile)
        val logoPart = MultipartBody.Part.createFormData("logo", logoFile.name, requestFile)

        // Make the API call
        val call = apiService.uploadAdmin(id_Perusahaan,email,password,nama, tanggal_lahir,logoPart)

        // Execute the call asynchronously
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    // Handle the API response
                    Log.d("ApiResponse", "Status: ${apiResponse?.status}, Message: ${apiResponse?.message}")
                } else {
                    Log.e("ApiResponse", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("ApiResponse", "Request failed: ${t.message}")
            }
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_CAPTURE_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        // Set the captured image to the CircleImageView
                        circleImageView.setImageBitmap(it)
                        // Convert the Bitmap to File
                        selectedFile = convertBitmapToFile(it)
                    }
                }
            }
            PICK_IMAGE_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let { imageUri: Uri ->
                        val realPath = getRealPathFromUri(imageUri)
                        if (realPath != null) {
                            selectedFile = File(realPath)
                            circleImageView.setImageURI(imageUri)
                        }
                    }
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
    private fun convertBitmapToFile(bitmap: Bitmap): File {
        // Create a temporary file
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)

        // Write the bitmap data into the file
        val outputStream = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        return tempFile
    }
    private fun showImagePickerDialog() {
        val options = arrayOf("Capture from Camera", "Select from File")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openFilePicker()
            }
        }
        alertDialog = builder.create()
        alertDialog.show()
    }
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_CAPTURE_REQUEST)
        } else {
            // Handle the case where the camera app is not available
            Toast.makeText(this, "Camera app not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBundle() {
        bundle = intent?.getBundleExtra("data")
        if (bundle != null) {
            bundle?.let {
                perusahaan = it.getParcelable("perusahaan")
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}