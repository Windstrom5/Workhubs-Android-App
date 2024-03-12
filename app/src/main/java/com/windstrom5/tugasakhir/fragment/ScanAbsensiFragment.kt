package com.windstrom5.tugasakhir.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.zxing.BinaryBitmap
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.activity.UserActivity
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import com.windstrom5.tugasakhir.model.SecretKeyInfo
import org.json.JSONException
import org.json.JSONObject
import com.windstrom5.tugasakhir.connection.Tracking
import com.windstrom5.tugasakhir.feature.QRCodeAnalyzer
import com.windstrom5.tugasakhir.model.Absen
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.ScanQRCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanAbsensiFragment : Fragment() {
    val scanCustomCode = registerForActivityResult(ScanCustomCode(), ::handleResult)
    private lateinit var requestQueue: RequestQueue // Add this line
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var button : Button
    private lateinit var logo : ImageView
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private var perusahaan: Perusahaan? = null
    private var pekerja: Pekerja? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan_absensi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLocationPermission()
        button = view.findViewById(R.id.absen)
        cameraExecutor = Executors.newSingleThreadExecutor()
        logo = view.findViewById(R.id.logoImage)
        requestQueue = Volley.newRequestQueue(requireContext())
        getBundle()
        val imageUrl =
            "https://df0f-125-163-245-254.ngrok-free.app/storage/${perusahaan?.logo}" // Replace with your Laravel image URL

        Glide.with(this)
            .load(imageUrl)
            .into(logo)

        button.setOnClickListener{
            scanCustomCode.launch(
                ScannerConfig.build {
                    setOverlayStringRes(R.string.scan) // string resource used for the scanner overlay
                }
            )
        }
    }

    fun handleResult(result: QRResult) {
        // handle the QRResult
        val resultString = result.toString()

        // Extract rawValue using a simple substring or regex
        val rawValue = extractRawValue(resultString)
        // Use the extracted rawValue in your further processing
        getAllSecretKeysFromApi(rawValue)
    }

    private fun extractRawValue(resultString: String): String {
        // Example: QRSuccess(content=Plain(rawBytes=[...], rawValue=...))
        val regex = Regex("rawValue=(\\w+)\\)")
        val matchResult = regex.find(resultString)
        return matchResult?.groups?.get(1)?.value ?: ""
    }
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, get user's location
            getUserLocation()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get user's location
                getUserLocation()
            } else {
                // Permission denied, handle accordingly
                // You can show a message or request the permission again
            }
        }
    }
    private fun getUserLocation() {
        val locationManager =  requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            // Get the last known location from the GPS provider
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude

            } else {
                // Handle the case where location is not available
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun getAllSecretKeysFromApi(qrCode: String) {
        val apiUrl = "https://df0f-125-163-245-254.ngrok-free.app/api/getAllSecretKeys"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { response ->
                val secretKeysList = mutableListOf<SecretKeyInfo>()
                val perusahaanArray = response.getJSONArray("perusahaan")
                for (i in 0 until perusahaanArray.length()) {
                    val perusahaanObject = perusahaanArray.getJSONObject(i)
                    val namaPerusahaan = perusahaanObject.getString("nama")
                    val secretKey = perusahaanObject.getString("secret_key")
                    val jamMasuk = perusahaanObject.getString("jam_masuk")
                    val jamKeluar = perusahaanObject.getString("jam_keluar")
                    secretKeysList.add(SecretKeyInfo(namaPerusahaan, secretKey, jamMasuk, jamKeluar))
                }
                // Compare QR code with API secret keys
                val isValidQRCode = compareSecretKeys(secretKeysList, qrCode)
                // Handle the result
                if (isValidQRCode) {
                    perusahaan?.let { pekerja?.let { it1 -> Absen(it, it1) } }
                } else {
                    MotionToast.createToast(requireActivity(), "Error",
                        "QR CODE INVALID",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.ralewaybold))
                }
            },
            { error ->
                error.message?.let { Log.d("testing", it) }
            })

        requestQueue.add(jsonObjectRequest)
    }

    private fun compareSecretKeys(apiSecretKeys: List<SecretKeyInfo>, qrCode: String): Boolean {
        for ((namaPerusahaan, secretKey, jamMasuk, jamKeluar) in apiSecretKeys) {
            val hashedSecretKey = md5(secretKey)
            if (hashedSecretKey == qrCode && namaPerusahaan == perusahaan?.nama) {
                Toast.makeText(requireContext(), "Valid QR Code for $namaPerusahaan", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        // Invalid QR Code
        return false
    }

    // Check and request location permission
    private fun Absen(perusahaan: Perusahaan, pekerja: Pekerja){
        val url = "https://df0f-125-163-245-254.ngrok-free.app/api/Absensi"
        Log.d("testing",url)
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time)
        val params = JSONObject()
        params.put("nama", pekerja.nama)
        params.put("perusahaan", perusahaan.nama)
        params.put("tanggal", currentDate)
        params.put("jam", currentTime)
        params.put("latitude", latitude)
        params.put("longitude", longitude)
        Log.d("testing",url)
        val request = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                Log.d("testing",url)
                try {
                    val message = response.getString("message")
                    // Process the status and message accordingly
                    when (message) {
                        "Absen Started" -> {
                            Log.d("testing3", "Done")
                            val startServiceIntent = Intent(requireActivity(), Tracking::class.java)
                            requireActivity().startService(startServiceIntent)
                            requireActivity().runOnUiThread {
                                MotionToast.createToast(
                                    requireActivity(),
                                    "Absen Startrd",
                                    "Happy Working",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        requireContext(),
                                        www.sanju.motiontoast.R.font.helveticabold
                                    )
                                )
                            }
                        }

                        "Absen Ended" -> {
                            Log.d("testing2",url)
                            val serviceIntent = Intent(requireContext(), Tracking::class.java)
                            requireContext().stopService(serviceIntent)
                            requireActivity().runOnUiThread {
                                MotionToast.createToast(
                                    requireActivity(),
                                    "Absen Completed",
                                    "Have A Nice Day",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        requireContext(),
                                        www.sanju.motiontoast.R.font.helveticabold
                                    )
                                )
                            }
                        }
                        else -> {
                            Log.d("testing1",url)
                            requireActivity().runOnUiThread {
                                MotionToast.createToast(
                                    requireActivity(),
                                    "Absen Failed",
                                    "You can only absen within 15 minutes of the scheduled time. Please try again.",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        requireContext(),
                                        www.sanju.motiontoast.R.font.helveticabold
                                    )
                                )
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.d("testing",url)
                }
            },
            { error ->
                // Handle error
                error.printStackTrace()

            }
        )
        requestQueue.add(request)
    }
    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
    private fun getBundle() {
        val arguments = arguments
        if (arguments != null) {
            perusahaan = arguments.getParcelable("perusahaan")
            pekerja = arguments.getParcelable("user")
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
    private fun getHtmlTemplate(absen: Absen, perusahaan: Perusahaan, pekerja: Pekerja): String {
        // Define your HTML template with placeholders for data
        val template = """
            <!DOCTYPE html>
            <html>
            
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                    }
            
                    .company-name {
                        font-weight: bold;
                        font-size: 1.2em;
                    }
            
                    .company-logo {
                        max-width: 100px; /* Set a maximum width for the logo */
                        max-height: 100px; /* Set a maximum height for the logo */
                    }
                </style>
            </head>
            <body>
                <h2 class="company-name">Struk Bukti Absensi</h2>
                <img class="company-logo" src="path_to_your_logo_image" alt="Company Logo">
                <p><b>Nama Perusahaan:</b> ${perusahaan.nama}</p>
                <p><b>Nama Pekerja:</b> ${pekerja.nama}</p>
                <p><b>Jam Masuk:</b> ${absen.jamMasuk}</p>
                <p><b>Jam Keluar:</b> ${absen.jamKeluar}</p>
                <!-- Add more data as needed -->

                <p>Terima kasih telah melakukan absensi.</p>
            </body>
            </html>
        """.trimIndent()

        // Replace placeholders with actual data
        return template
    }
}

//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            val preview = Preview.Builder().build().also {
//                it.setSurfaceProvider(previewView.surfaceProvider)
//            }
//
//            val imageAnalysis = ImageAnalysis.Builder()
//                .setTargetResolution(Size(1920, 1080))
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .build()
//
//            imageAnalysis.setAnalyzer(cameraExecutor, QRCodeAnalyzer(scanningSquare) { qrCode ->
//                getAllSecretKeysFromApi(qrCode)
//            })
//
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview, imageAnalysis
//                )
//            } catch (exc: Exception) {
//                // Handle exceptions
//            }
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }
