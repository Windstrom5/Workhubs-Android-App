package com.windstrom5.tugasakhir.adapter

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.model.Absen
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import java.io.File
import java.io.FileOutputStream

class AbsenAdapter(
    private val absenList: List<Absen>,
    private val perusahaan: Perusahaan,
    private val pekerja: Pekerja
) : RecyclerView.Adapter<AbsenAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tanggalTextView: TextView = view.findViewById(R.id.tanggal)
        val jamTextView: TextView = view.findViewById(R.id.jam)
        val actionButton: Button = view.findViewById(R.id.actionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_absen, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val absen = absenList[position]

        // Bind data to views
        holder.tanggalTextView.text = absen.tanggal.toString()
        holder.jamTextView.text = absen.jamMasuk

        // Set click listener for the action button
        holder.actionButton.setOnClickListener {
            // Handle button click, e.g., initiate download
            val pdfHtml = getHtmlTemplate(absen, perusahaan, pekerja)
            generateAndDownloadPdf(holder.actionButton.context, pdfHtml)
        }
    }

    override fun getItemCount(): Int {
        return absenList.size
    }

    private fun generateAndDownloadPdf(context: Context, htmlContent: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val webView = WebView(context)
        webView.layout(0, 0, webView.width, webView.height)
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
        webView.draw(canvas)

        pdfDocument.finishPage(page)

        // Save the PDF file
        val file = File(context.cacheDir, "absen_pdf.pdf")
        val fileOutputStream = FileOutputStream(file)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()

        // Open the PDF file using an Intent
        val uri = FileProvider.getUriForFile(
            context,
            "your.package.name.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
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

