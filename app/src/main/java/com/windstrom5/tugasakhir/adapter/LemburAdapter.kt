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
import com.windstrom5.tugasakhir.model.Izin
import com.windstrom5.tugasakhir.model.Lembur
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LemburAdapter (
    private val lemburList: List<Lembur>,
    private val perusahaan: Perusahaan,
    private val pekerja: Pekerja
) : RecyclerView.Adapter<LemburAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tanggalTextView: TextView = view.findViewById(R.id.tanggal)
        val jamTextView: TextView = view.findViewById(R.id.jam)
        val actionButton: Button = view.findViewById(R.id.actionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_lembur, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lembur = lemburList[position]

        // Bind data to views
        holder.tanggalTextView.text = lembur.tanggal.toString()
        holder.jamTextView.text = "${lembur.waktu_masuk} - ${lembur.waktu_pulang}"

        // Set click listener for the action button
        holder.actionButton.setOnClickListener {
            // Handle button click, e.g., initiate download
            val pdfHtml = getHtmlTemplate(lembur, perusahaan, pekerja)
            generateAndDownloadPdf(holder.actionButton.context, pdfHtml)
        }
    }

    override fun getItemCount(): Int {
        return lemburList.size
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

    private fun getHtmlTemplate(lembur: Lembur, perusahaan: Perusahaan, pekerja: Pekerja): String {
        // Define your HTML template with placeholders for data
        val template = """
            <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Work Overtime Receipt for Employee</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            margin: 20px;
                            text-align: center;
                        }
                        header {
                            background-color: #4CAF50;
                            padding: 10px;
                            color: #fff;
                        }
                        h1 {
                            margin-bottom: 0;
                        }
                        .logo {
                            max-width: 100px;
                            margin: 10px auto;
                        }
                        .receipt-details {
                            margin-top: 20px;
                            text-align: left;
                        }
                        .receipt-details p {
                            margin: 5px 0;
                        }
                        footer {
                            margin-top: 50px;
                            padding-top: 10px;
                            border-top: 1px solid #ccc;
                            color: #555;
                        }
                    </style>
                </head>
                <body>
                    <header>
                        <h1>Receipt for Pekerja</h1>
                    </header>
                    <img src="[URL to Perusahaan Logo]" alt="Perusahaan Logo" class="logo">
                    <div class="receipt-details">\
                        <p><strong>Date Printed:</strong> ${
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Date()
            )}</p>
                    <p><strong>Company Name:</strong> ${perusahaan.nama}</p>
                    <p><strong>Worker Name:</strong> ${pekerja.nama}</p>
                    <p><strong>Date Worked:</strong> ${lembur.tanggal}</p>
                    <p><strong>Check-in Time:</strong> ${lembur.waktu_masuk}</p>
                    <p><strong>Check-out Time:</strong> ${lembur.waktu_pulang}</p>
                    <p><strong>Work Description:</strong> ${lembur.pekerjaan}</p>
                    </div>
                    <footer>
                        <p>Powered by Workhubs</p>
                    </footer>
                </body>
                </html>

        """.trimIndent()

        // Replace placeholders with actual data
        return template
    }

    private fun calculateTotalHours(startTime: String, endTime: String): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = format.parse(startTime)
        val end = format.parse(endTime)

        val diff = end.time - start.time
        val hours = diff / (60 * 60 * 1000)
        val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)

        return String.format(Locale.getDefault(), "%d hours %02d minutes", hours, minutes)
    }
}