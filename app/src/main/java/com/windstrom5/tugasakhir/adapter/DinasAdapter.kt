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
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.feature.PreviewDialogFragment
import com.windstrom5.tugasakhir.model.DinasItem
import com.windstrom5.tugasakhir.model.historyDinas
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DinasAdapter(
    private val context: Context,
    private val statusWithDinasList: List<historyDinas>,
    private val Role:String
) : BaseExpandableListAdapter() {
    private val rotationAngleExpanded = 180f
    private val rotationAngleCollapsed = 0f
    override fun getGroupCount(): Int {
        return statusWithDinasList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return statusWithDinasList[groupPosition].dinasList.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return statusWithDinasList[groupPosition].status
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return statusWithDinasList[groupPosition].dinasList[childPosition]
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

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val status = getGroup(groupPosition) as String
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_group, parent, false)
        view.findViewById<TextView>(R.id.title).text = status
        val arrowLogo = view.findViewById<ImageView>(R.id.arrowLogo)
        if (isExpanded) {
            arrowLogo.rotation = rotationAngleExpanded
        } else {
            arrowLogo.rotation = rotationAngleCollapsed
        }
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val dinas = getChild(groupPosition, childPosition) as DinasItem
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.history_dinas, parent, false)
        val tanggal = view.findViewById<TextView>(R.id.tanggal)
        val tujuan = view.findViewById<TextView>(R.id.tujuan)
        val actionButton = view.findViewById<Button>(R.id.actionButton)
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val tanggalBerangkatFormatted = dateFormatter.format(dinas.tanggal_berangkat)
        val tanggalPulangFormatted = dateFormatter.format(dinas.tanggal_pulang)
        tanggal.text = "$tanggalBerangkatFormatted - $tanggalPulangFormatted"
        tujuan.text = dinas.tujuan
        if (Role == "Admin"){
            val buttonText = when (dinas.status) {
                "Pending" -> {
                    actionButton.visibility = View.VISIBLE
                    "Respond \nDinas"
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
//                if (actionButton.text == "Download Receipt"){
//
//                }
            }
        }else{
            val buttonText = when (dinas.status) {
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
                if (actionButton.text == "Download Receipt"){
                    val htmlContent = getHtmlTemplate(dinas)
                    generatePdfFromHtml(htmlContent)
                }else if(actionButton.text == "Respond Dinas"){
                    val fragmentManager = (context as AppCompatActivity).supportFragmentManager
                    val previewDialogFragment = PreviewDialogFragment()
                    val bundle = Bundle()
                    bundle.putParcelable("dinas", dinas) // Pass different object for lembur
                    bundle.putString("layoutType", "dinas_layout") // Add layout type here
                    previewDialogFragment.arguments = bundle
                    previewDialogFragment.show(fragmentManager, "preview_dialog")
                }
            }
        }
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
    private fun getHtmlTemplate(dinas: DinasItem): String {
        val template = """
            <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Work Assignment Receipt for Employee</title>
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
                <p><strong>Company Name:</strong> ${dinas.nama_perusahaan}</p>
                <p><strong>Worker Name:</strong> ${dinas.nama_pekerja}</p>
                <p><strong>Date of Assignment:</strong></p>
                <p><strong>Departure Time:</strong> ${dinas.tanggal_berangkat}</p>
                <p><strong>Return Time:</strong> ${dinas.tanggal_pulang}</p>
                <p><strong>Assigned Activity:</strong> ${dinas.kegiatan}</p>
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




