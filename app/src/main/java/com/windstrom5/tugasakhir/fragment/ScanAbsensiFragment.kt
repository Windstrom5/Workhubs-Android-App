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
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
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
import com.google.zxing.BinaryBitmap
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import com.windstrom5.tugasakhir.model.SecretKeyInfo
import org.json.JSONException
import org.json.JSONObject
import com.windstrom5.tugasakhir.connection.Tracking
import com.windstrom5.tugasakhir.model.Absen
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanAbsensiFragment : Fragment() {
    private lateinit var requestQueue: RequestQueue // Add this line
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView:PreviewView
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private var perusahaan : Perusahaan? = null
    private var pekerja : Pekerja? = null
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
        previewView = view.findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()
        requestQueue = Volley.newRequestQueue(requireContext())
        getBundle()
        startCamera()
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
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1920, 1080))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, QRCodeAnalyzer { qrCode ->
                getAllSecretKeysFromApi(qrCode)
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                // Handle exceptions
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private class QRCodeAnalyzer(private val onQrCodeScanned: (String) -> Unit) :
        ImageAnalysis.Analyzer {
        private val reader = QRCodeReader()

        override fun analyze(imageProxy: ImageProxy) {
            val buffer = imageProxy.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val source = PlanarYUVLuminanceSource(
                data,
                imageProxy.width,
                imageProxy.height,
                0,
                0,
                imageProxy.width,
                imageProxy.height,
                false
            )

            val bitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result: Result = reader.decode(bitmap)
                onQrCodeScanned(result.text)
            } catch (e: ReaderException) {
                // QR code not found in the image
            }

            imageProxy.close()
        }
    }

    private fun getAllSecretKeysFromApi(qrCode: String) {
        val apiUrl = "http://127.0.0.1:8000/api/getAllSecretKeys"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, apiUrl, null,
            { response ->

                val secretKeysList = mutableListOf<SecretKeyInfo>()

                for (i in 0 until response.length()) {
                    val jsonObject = response.getJSONObject(i)
                    val namaPerusahaan = jsonObject.getString("namaperusahaan")
                    val secretKey = jsonObject.getString("secret_key")
                    val jamMasuk = jsonObject.getString("jam_masuk")
                    val jamKeluar = jsonObject.getString("jam_keluar")
                    secretKeysList.add(SecretKeyInfo(namaPerusahaan, secretKey, jamMasuk, jamKeluar))
                }
                compareSecretKeys(secretKeysList, qrCode)
            },
            { error ->
                // Handle error cases
            })

        requestQueue.add(jsonArrayRequest)
    }

    private fun compareSecretKeys(apiSecretKeys: List<SecretKeyInfo>, qrCode: String) {
        val hashedQrCode = md5(qrCode)

        for ((namaPerusahaan, secretKey, jamMasuk, jamKeluar) in apiSecretKeys) {
            val hashedSecretKey = md5(secretKey)
            if (hashedSecretKey == hashedQrCode) {
                if(namaPerusahaan == perusahaan?.nama){
                    perusahaan?.let { pekerja?.let { it1 -> Absen(it, it1) } }
                    Toast.makeText(requireContext(), "Valid QR Code for $namaPerusahaan", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        // QR code is invalid
        // Handle accordingly
        Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show()
    }
    // Check and request location permission
    private fun Absen(perusahaan: Perusahaan, pekerja: Pekerja){
        checkLocationPermission()
        val url = "http://127.0.0.1:8000/absen"
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time)
        val params = JSONObject()
        try {
            params.put("nama", pekerja.nama)
            params.put("perusahaan", perusahaan.nama)
            params.put("tanggal", currentDate)
            params.put("jam_masuk", currentTime)
            params.put("jam_keluar", currentTime)
            params.put("latitude", latitude)
            params.put("longitude", longitude)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val request = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                fun onResponse(response: JSONObject) {
                    // Handle the response from the server
                    try {
                        val status = response.getString("status")
                        val message = response.getString("message")

                        // Process the status and message accordingly
                        if ("success" == status) {
                            if(message == "Absen Started"){
//                                val broadcastIntent = Intent("com.windstrom5.tugasakhir.ACTION_LOCATION_UPDATE")
//                                broadcastIntent.putExtra("latitude", latitude)
//                                broadcastIntent.putExtra("longitude", longitude)
//                                context?.sendBroadcast(broadcastIntent)
                                val startServiceIntent = Intent(requireActivity(), Tracking::class.java)
                                requireActivity().startService(startServiceIntent)
                                MotionToast.createToast(
                                    requireActivity(),
                                    "Absen Startrd",
                                    "Happy Working",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(requireContext(), www.sanju.motiontoast.R.font.helveticabold)
                                )
                            }else{
                                val serviceIntent = Intent(requireContext(), Tracking::class.java)
                                requireContext().stopService(serviceIntent)
                                MotionToast.createToast(
                                    requireActivity(),
                                    "Absen Completed",
                                    "Have A Nice Day",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(requireContext(), www.sanju.motiontoast.R.font.helveticabold)
                                )
                            }

                        } else {
                            // Handle error
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
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
