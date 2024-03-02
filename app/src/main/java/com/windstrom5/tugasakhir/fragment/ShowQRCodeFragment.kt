package com.windstrom5.tugasakhir.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.connection.SharedPreferencesManager
import com.windstrom5.tugasakhir.model.Perusahaan
import java.util.Hashtable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.security.MessageDigest

class ShowQRCodeFragment : Fragment() {
    private lateinit var imageViewQRCode: ImageView
    private lateinit var perusahaan: Perusahaan

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_q_r_code, container, false)
        imageViewQRCode = view.findViewById(R.id.imageViewQRCode)

        // Get Perusahaan object from SharedPreferences
        perusahaan = getPerusahaan() ?: return view

        // Generate QR code with logo
        generateQRCodeWithLogo(perusahaan)

        return view
    }

    private fun generateQRCodeWithLogo(perusahaan: Perusahaan) {
        try {
            // Generate QR code with QRCodeWriter
            val secretKeyMD5 = md5(perusahaan.secret_key)
            val qrCodeBitmap = generateQRCode(secretKeyMD5, 400, 400)

            // Add logo to the center of the QR code
            val finalBitmap = addLogoToQRCode(qrCodeBitmap, perusahaan.logo)

            // Display the final QR code with the logo
            imageViewQRCode.setImageBitmap(finalBitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    @Throws(WriterException::class)
    private fun generateQRCode(content: String, width: Int, height: Int): Bitmap {
        val hints: Hashtable<EncodeHintType, Any> = Hashtable()
        hints[EncodeHintType.MARGIN] = 0 // Adjust margin as needed
        val bitMatrix: BitMatrix =
            MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) -0x1000000 else -0x1
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun addLogoToQRCode(qrCodeBitmap: Bitmap, logoLink: String): Bitmap {
        val centerX = qrCodeBitmap.width / 2.0f
        val centerY = qrCodeBitmap.height / 2.0f
        val finalBitmap = Bitmap.createBitmap(
            qrCodeBitmap.width,
            qrCodeBitmap.height,
            qrCodeBitmap.config
        )
        val canvas = android.graphics.Canvas(finalBitmap)
        canvas.drawBitmap(qrCodeBitmap, 0f, 0f, null)
        Glide.with(this)
            .asBitmap()
            .load(logoLink)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Calculate the position to place the logo at the center
                    val left = (centerX - resource.width / 2).toInt()
                    val top = (centerY - resource.height / 2).toInt()

                    // Draw the logo on the canvas
                    canvas.drawBitmap(resource, left.toFloat(), top.toFloat(), null)

                    // Set the final bitmap to the ImageView
                    imageViewQRCode.setImageBitmap(finalBitmap)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle when the image loading is cleared
                }
            })

        return finalBitmap
    }
    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun getPerusahaan(): Perusahaan? {
        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        return sharedPreferencesManager.getPerusahaan()
    }
}
