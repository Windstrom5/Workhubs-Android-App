package com.windstrom5.tugasakhir.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.connection.ApiResponse
import com.windstrom5.tugasakhir.connection.ApiService
import com.windstrom5.tugasakhir.connection.SharedPreferencesManager
import com.windstrom5.tugasakhir.databinding.ActivityRegisterAdminBinding
import com.windstrom5.tugasakhir.databinding.ActivityRegisterPekerjaBinding
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import com.windstrom5.tugasakhir.model.response
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegisterPekerjaActivity : AppCompatActivity() {
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
    private lateinit var pekerja: Pekerja
    private var perusahaan : Perusahaan? = null
    private lateinit var selectedFile: File
    private var admin : Admin? = null
    private lateinit var acKategori : AutoCompleteTextView
    private lateinit var binding: ActivityRegisterPekerjaBinding
    private lateinit var circleImageView: CircleImageView
    private lateinit var selectedImage: ByteArray
    var selectedDateSqlFormat: String? = null
    private lateinit var selectImage: ImageView
    private lateinit var logo : ImageView
    private val CAMERA_PERMISSION_REQUEST = 124
    private val CAMERA_CAPTURE_REQUEST = 126
    private var idPerusahaan: Int? = null
    private var tempImageFile: File? = null
    private lateinit var loading : LinearLayout
    private lateinit var namaPerusahaan: String
    private val PICK_IMAGE_REQUEST_CODE = 1
    private lateinit var requestQueue: RequestQueue
    private var kategoriPekerja: List<String> =
        mutableListOf("Admin","Pekerja");
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPekerjaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getBundle()
        requestQueue = Volley.newRequestQueue(this)
        circleImageView = binding.circleImageView
        TINama = binding.textInputNama
        selectImage = binding.selectImage
        loading = findViewById(R.id.layout_loading)
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
        TITanggal.setEndIconOnClickListener{
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
        val email = binding.textInputEmail.editText?.text.toString().trim()
        if(TINama == null || TIEmail == null || TIPassword == null || TITanggal == null){
            MotionToast.createToast(this@RegisterPekerjaActivity, "Error",
                "Ada Form Yang belum Terisi",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this@RegisterPekerjaActivity, R.font.ralewaybold))
        }else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setLoading(true)
            val url = "http://192.168.1.4:8000/api/"
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            val call = apiService.checkEmail(email)
            call.enqueue(object : Callback<response> {
                override fun onResponse(call: Call<response>, response: Response<response>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.pekerja == null && apiResponse?.admin == null) {
                            perusahaan?.let { it1 -> saveData(it1) }
                        } else {
                            runOnUiThread{
                                setLoading(false)
                                MotionToast.createToast(this@RegisterPekerjaActivity, "Error",
                                    "Email Sudah Digunakan",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(this@RegisterPekerjaActivity, R.font.ralewaybold))
                            }
                        }
                    } else {
                        // Handle unsuccessful response
                    }
                    setLoading(false)
                }

                override fun onFailure(call: Call<response>, t: Throwable) {
                    // Handle failure
                    setLoading(false)
                }
            })

        } else {
            // Email format is incorrect
            // Handle accordingly
        }
    }

    private fun setLoading(isLoading:Boolean){
        if(isLoading){
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            loading!!.visibility = View.VISIBLE
        }else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            loading!!.visibility = View.INVISIBLE
        }
    }

    private fun saveData(perusahaan: Perusahaan){
        val url = "http://192.168.1.4:8000/api/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        val nama_perusahaan = createPartFromString(perusahaan.nama.toString())
        val emailRequestBody = createPartFromString(TIEmail.editText?.text.toString())
        val passwordRequestBody = createPartFromString(TIPassword.editText?.text.toString())
        val nama = createPartFromString(TINama.editText?.text.toString())
        val tanggalRequestBody = createPartFromString(selectedDateSqlFormat.toString())
        val profilePath = selectedFile
        val requestFile = RequestBody.create(MediaType.parse("image/*"), profilePath)
        val profilepath = MultipartBody.Part.createFormData("profile", profilePath.name, requestFile)
        val call = apiService.uploadPekerja(nama_perusahaan, emailRequestBody, passwordRequestBody, nama,tanggalRequestBody,
            profilepath)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val path = apiResponse?.profile_path ?: ""
                    val id_Pekerja = apiResponse?.id ?: 0
                    MotionToast.createToast(this@RegisterPekerjaActivity,
                        "Success",
                        "Berhasil Menyimpan Data",
                        MotionToastStyle.SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this@RegisterPekerjaActivity,
                            R.font.ralewaybold))
                    val intent = Intent(this@RegisterPekerjaActivity, CompanyActivity::class.java)
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    val formattedDate = TITanggal.editText?.text.toString()
                    try {
                        val parsedDate: Date? = dateFormat.parse(formattedDate)
                        val dateFormatSql = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDateSql = dateFormatSql.format(parsedDate)
                        val parsedDateSql: Date? = dateFormatSql.parse(formattedDateSql)
                        perusahaan.id?.let {
                            if (parsedDateSql != null) {
                                pekerja = Pekerja(
                                    id_Pekerja,
                                    it,
                                    TIEmail.editText?.text.toString(),
                                    TIPassword.editText?.text.toString(),
                                    TINama.editText?.text.toString(),
                                    parsedDateSql,
                                    path
                                )
                            }
                        }
                        val sharedPreferencesManager = SharedPreferencesManager(this@RegisterPekerjaActivity)
                        sharedPreferencesManager.savePekerja(pekerja)
                        val userBundle = Bundle()
                        userBundle.putParcelable("user", admin)
                        userBundle.putParcelable("perusahaan", perusahaan)
                        intent.putExtra("data", userBundle)
                        startActivity(intent)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        // Handle parsing exception
                    }

                } else {
                    MotionToast.createToast(this@RegisterPekerjaActivity, "Error",
                        "Tidak Dapat Menyimpan Data",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this@RegisterPekerjaActivity, R.font.ralewaybold))
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                MotionToast.createToast(
                    this@RegisterPekerjaActivity,
                    "Failure",
                    "Something went wrong",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this@RegisterPekerjaActivity, R.font.ralewaybold)
                )
            }
        })
    }

    private fun createPartFromString(value: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), value)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
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
                admin = it.getParcelable("user")
                val imageUrl =
                    "http://192.168.1.4:8000/storage/${perusahaan?.logo}" // Replace with your Laravel image URL
                val profileImageView = binding.logo
                Glide.with(this)
                    .load(imageUrl)
                    .into(profileImageView)
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}