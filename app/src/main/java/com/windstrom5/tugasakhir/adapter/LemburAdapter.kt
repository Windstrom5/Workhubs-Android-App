package com.windstrom5.tugasakhir.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.feature.PreviewDialogFragment
import com.windstrom5.tugasakhir.model.LemburItem
import com.windstrom5.tugasakhir.model.historyLembur
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter

class LemburAdapter(
    private val context: Context,
    private val statusWithLemburList: List<historyLembur>,
    private val Role:String
) : BaseExpandableListAdapter() {
    private val rotationAngleExpanded = 180f
    private val rotationAngleCollapsed = 0f
    override fun getGroupCount(): Int {
        return statusWithLemburList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return statusWithLemburList[groupPosition].lemburList.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return statusWithLemburList[groupPosition].status
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return statusWithLemburList[groupPosition].lemburList[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val status = getGroup(groupPosition) as String
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.list_group, parent, false)
        view.findViewById<TextView>(R.id.title).text = status
        val arrowLogo = view.findViewById<ImageView>(R.id.arrowLogo)
        if (isExpanded) {
            arrowLogo.rotation = rotationAngleExpanded
        } else {
            arrowLogo.rotation = rotationAngleCollapsed
        }
        return view
    }
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val lembur = getChild(groupPosition, childPosition) as LemburItem
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.history_lembur, parent, false)
        val tanggal = view.findViewById<TextView>(R.id.tanggal)
        val jam = view.findViewById<TextView>(R.id.jam)
        val actionButton = view.findViewById<Button>(R.id.actionButton)
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val timeFormatter = SimpleDateFormat("HH:mm", Locale("id", "ID")) // Use "HH:mm" for 24-hour format
        val tanggalFormatted = dateFormatter.format(lembur.tanggal)
        val jammasuk = timeFormatter.format(lembur.waktu_masuk)
        val jamkeluar = timeFormatter.format(lembur.waktu_pulang)
        tanggal.text = tanggalFormatted
        jam.text = "$jammasuk - $jamkeluar"
        if (Role == "Admin") {
            val buttonText = when (lembur.status) {
                "Pending" -> {
                    actionButton.visibility = View.VISIBLE
                    "Respond \nLembur"
                }

                "Accept" -> {
                    actionButton.visibility = View.VISIBLE
                    "View \nData"
                }

                else -> {
                    actionButton.visibility = View.GONE
                    "View \nData"
                }
            }
            actionButton.text = buttonText

            actionButton.setOnClickListener {
                if (actionButton.text == "Download Receipt") {
                    getHtmlTemplate(lembur)
                }
            }
        } else {
            val buttonText = when (lembur.status) {
                "Pending" -> {
                    actionButton.visibility = View.VISIBLE
                    "Edit \nData"
                }

                "Accept" -> {
                    actionButton.visibility = View.VISIBLE
                    "Download \nReceipt"
                }

                else -> {
                    actionButton.visibility = View.GONE
                    "View \nData"
                }
            }
            actionButton.text = buttonText
            actionButton.setOnClickListener {
                if (actionButton.text == "Download Receipt") {
                    val htmlContent = getHtmlTemplate(lembur)
                    generatePdfFromHtml(htmlContent)
                } else if (actionButton.text == "Respond Lembur") {
                    val fragmentManager = (context as AppCompatActivity).supportFragmentManager
                    val previewDialogFragment = PreviewDialogFragment()
                    val bundle = Bundle()
                    bundle.putParcelable("lembur",lembur) // Pass different object for lembur
                    bundle.putString("layoutType", "lembur_layout") // Add layout type here
                    previewDialogFragment.arguments = bundle
                    previewDialogFragment.show(fragmentManager, "preview_dialog")
                }
            }
        }
        return view
    }

    private fun getHtmlTemplate(lembur: LemburItem): String {
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
            )
        }</p>
                    <p><strong>Company Name:</strong> ${lembur.nama_perusahaan}</p>
                    <p><strong>Worker Name:</strong> ${lembur.nama_pekerja}</p>
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
    private fun generatePdfFromHtml(htmlContent: String) {
        val outputPdfFile = File(context.getExternalFilesDir(null), "receipt.pdf")
        val outputStream = FileOutputStream(outputPdfFile)
        val converterProperties = ConverterProperties()
        HtmlConverter.convertToPdf(htmlContent, outputStream, converterProperties)
        outputStream.close()

        // PDF is generated, you can now save it or share it as needed
    }
}
