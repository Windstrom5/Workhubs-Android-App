package com.windstrom5.tugasakhir.fragment

import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.model.SecretKeyInfo
import java.security.MessageDigest
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanAbsensiFragment : Fragment() {
    private lateinit var requestQueue: RequestQueue // Add this line
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView:PreviewView
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
        startCamera()
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
        val apiUrl = "https://790e-36-80-222-40.ngrok-free.app/api/getAllSecretKeys"

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
                // QR code is valid
                // Proceed with your logic using namaPerusahaan, jamMasuk, and jamKeluar
                Toast.makeText(requireContext(), "Valid QR Code for $namaPerusahaan", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // QR code is invalid
        // Handle accordingly
        Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show()
    }
    private fun checkAbsen(namaPerusahaan: String, email: String, password: String){

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
}
