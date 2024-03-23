package com.windstrom5.tugasakhir.feature

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.google.android.material.textfield.TextInputLayout
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.model.DinasItem
import com.windstrom5.tugasakhir.model.IzinItem
import com.windstrom5.tugasakhir.model.LemburItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

class PreviewDialogFragment: DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout based on the layout type passed in arguments
        val layoutType = arguments?.getString("layoutType")
        val view = when (layoutType) {
            "dinas_layout" -> inflater.inflate(R.layout.preview_dinas, container, false)
            "lembur_layout" -> inflater.inflate(R.layout.preview_lembur, container, false)
            else -> inflater.inflate(R.layout.preview_izin, container, false)
        }
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dinas = arguments?.getParcelable("dinas") as DinasItem?
        val lembur = arguments?.getParcelable("lembur") as LemburItem?
        val izin = arguments?.getParcelable("izin") as IzinItem?
        if (dinas != null) {
            view.findViewById<TextInputLayout>(R.id.namaInputLayout).editText?.setText(dinas.nama_pekerja)
            view.findViewById<TextInputLayout>(R.id.tanggalInputLayout).editText?.setText("${dinas.tanggal_berangkat} - ${dinas.tanggal_pulang}")
            view.findViewById<TextInputLayout>(R.id.tujuanInputLayout).editText?.setText(dinas.tujuan)
            view.findViewById<TextInputLayout>(R.id.kegiatanInputLayout).editText?.setText(dinas.kegiatan)

            val pdfUrl = "https://zgwlhkjfnp.sharedwithexpose.com/storage/${dinas.bukti}"
            val pdfView = view.findViewById<PDFView>(R.id.pdfView)
            pdfView.fromUri(Uri.parse(pdfUrl))
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .scrollHandle(DefaultScrollHandle(requireContext()))
                .load()

            // Set click listeners for accept and reject buttons if needed
            // Example:
            view.findViewById<Button>(R.id.acceptButton).setOnClickListener {
                // Perform accept action
            }

            view.findViewById<Button>(R.id.rejectButton).setOnClickListener {
                dismiss()
            }
        } else if (lembur != null) {
            view.findViewById<TextInputLayout>(R.id.namaInputLayout).editText?.setText(lembur.nama_pekerja)
            view.findViewById<TextInputLayout>(R.id.tanggalInputLayout).editText?.setText(lembur.tanggal.toString())
            view.findViewById<TextInputLayout>(R.id.jamInputLayout).editText?.setText("${lembur.waktu_masuk} - ${lembur.waktu_pulang}")
            view.findViewById<TextInputLayout>(R.id.kegiatanEditText).editText?.setText(lembur.pekerjaan)

            val imageView = view.findViewById<ImageView>(R.id.imageView)
            // Load image using Glide or Picasso
            // Example with Glide:
            Glide.with(requireContext())
                .load("https://zgwlhkjfnp.sharedwithexpose.com/storage/${lembur.bukti}")
                .into(imageView)
            view.findViewById<Button>(R.id.acceptButton).setOnClickListener {
                // Perform accept action
            }

            view.findViewById<Button>(R.id.rejectButton).setOnClickListener {
                dismiss()
            }
        }else if (izin != null){
            view.findViewById<TextInputLayout>(R.id.namaInputLayout).editText?.setText(izin.nama_pekerja)
            view.findViewById<TextInputLayout>(R.id.tanggalInputLayout).editText?.setText(izin.tanggal.toString())
            view.findViewById<TextInputLayout>(R.id.kategoriInputLayout).editText?.setText(izin.kategori)
            view.findViewById<TextInputLayout>(R.id.kegiatanInputLayout).editText?.setText(izin.alasan)

            val attachmentUrl = "https://5a81-36-81-23-4.ngrok-free.app/storage/${Uri.encode(izin.bukti)}"
            val isPdf = attachmentUrl.endsWith(".pdf")

            if (isPdf) {
                val webView = view.findViewById<WebView>(R.id.webView)
                webView.visibility = View.VISIBLE
// Load PDF into WebView
                webView.settings.javaScriptEnabled = true // Enable JavaScript if required
                webView.settings.allowFileAccessFromFileURLs = true // Allow access to file URLs
                webView.settings.allowUniversalAccessFromFileURLs = true // Allow access to file URLs on Android 8+
                // Load the PDF directly without going through the ngrok warning page
                val headers = mapOf("ngrok-skip-browser-warning" to "true")
                webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$attachmentUrl")
//                val pdfFileName = "${context?.cacheDir}/${System.currentTimeMillis()}.pdf"
//                val file = File(pdfFileName)
//
//                // Download the PDF file
//                CoroutineScope(Dispatchers.IO).launch {
//                    try {
//                        val inputStream = URL(attachmentUrl).openStream()
//                        val outputStream = FileOutputStream(file)
//                        inputStream.copyTo(outputStream)
//                        outputStream.close()
//
//                        // Load the downloaded PDF file into PDFView
//                        withContext(Dispatchers.Main) {
//                            val pdfView = view.findViewById<PDFView>(R.id.pdfView)
//                            pdfView.visibility = View.VISIBLE
//                            pdfView.fromFile(file)
//                                .defaultPage(0)
//                                .enableSwipe(true)
//                                .swipeHorizontal(false)
//                                .enableDoubletap(true)
//                                .load()
//                        }
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                        // Handle download or loading errors
//                    }
//                }
            } else {
                // Load image using Glide or Picasso
                val imageView = view.findViewById<ImageView>(R.id.imageView)
                imageView.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(attachmentUrl)
                    .into(imageView)
            }
            view.findViewById<Button>(R.id.acceptButton).setOnClickListener {
                // Perform accept action
            }

            view.findViewById<Button>(R.id.rejectButton).setOnClickListener {
                dismiss()
            }
        }else{
            Toast.makeText(requireContext(),"Failed Open The Dialog",Toast.LENGTH_LONG).show()
        }
    }

}