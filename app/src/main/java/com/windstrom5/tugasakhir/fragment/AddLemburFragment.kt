package com.windstrom5.tugasakhir.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import java.util.Calendar
import java.util.Locale
import android.content.res.Resources
import android.widget.TimePicker
import java.sql.Time
import java.util.*
class AddLemburFragment : Fragment() {
    // Define your views
    private lateinit var edNama: TextInputEditText
    private lateinit var edTanggal: TextInputEditText
    private lateinit var edMasuk: TextInputEditText
    private lateinit var edPulang: TextInputEditText
    private lateinit var edpekerjaan: TextInputEditText
    private lateinit var uploadfileButton: Button
    private lateinit var selectedFileName: TextView
    private lateinit var changeFileButton: Button
    private lateinit var cirsaveButton: Button
    private var perusahaan : Perusahaan? = null
    private var pekerja : Pekerja? = null
    private lateinit var TIKeluar: TextInputLayout
    private lateinit var TIMasuk: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_lembur, container, false)
        getBundle()
        TIKeluar = view.findViewById(R.id.TIKeluar)
        edNama = view.findViewById(R.id.edNama)
        edTanggal = view.findViewById(R.id.edTanggal)
        edMasuk = view.findViewById(R.id.edMasuk)
        edPulang = view.findViewById(R.id.edPulang)
        edpekerjaan = view.findViewById(R.id.edpekerjaan)
        uploadfileButton = view.findViewById(R.id.uploadfile)
        selectedFileName = view.findViewById(R.id.selectedFileName)
        changeFileButton = view.findViewById(R.id.changeFile)
        cirsaveButton = view.findViewById(R.id.cirsaveButton)
        TIMasuk.setEndIconOnClickListener{
            perusahaan?.let { it1 -> showTimePickerDialog(TIMasuk, it1) }
        }
        TIKeluar.setEndIconOnClickListener {
            perusahaan?.let { it1 -> showTimePickerDialog(TIKeluar, it1) }
        }
        uploadfileButton.setOnClickListener {
            // Implement file upload logic here
            // You can use Intent.ACTION_GET_CONTENT to open a file picker
            // and get the selected file's URI
            // Once you have the file URI, you can update the selectedFileName TextView
        }

        // Set onClickListener for the change file button
        changeFileButton.setOnClickListener {
            // Implement logic to change the selected file
        }

        // Set onClickListener for the save button
        cirsaveButton.setOnClickListener {
            // Implement logic to save the form data
            // You can access the entered data using the views like edNama.text.toString(), etc.
        }

        return view
    }
    private fun showTimePickerDialog(textInputLayout: TextInputLayout, perusahaan: Perusahaan) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                textInputLayout.editText?.setText(selectedTime)
            },
            currentHour,
            currentMinute,
            true
        )

        // Set the time range for the TimePickerDialog
        timePickerDialog.updateTime(currentHour, currentMinute)
        timePickerDialog.setRange(perusahaan.jam_masuk, perusahaan.jam_keluar)

        timePickerDialog.show()
    }

    private fun TimePickerDialog.setRange(minTime: Time, maxTime: Time) {
        try {
            val timePicker = this.findViewById<TimePicker>(
                Resources.getSystem().getIdentifier("timePicker", "id", "android")
            )

            val field = TimePicker::class.java.getDeclaredField("mDelegate")
            field.isAccessible = true
            val delegate = field.get(timePicker)
            val method = delegate.javaClass.getDeclaredMethod(
                "setHour", Int::class.javaPrimitiveType
            )
            method.isAccessible = true
            method.invoke(delegate, minTime.hours)

            val method2 = delegate.javaClass.getDeclaredMethod(
                "setMinute", Int::class.javaPrimitiveType
            )
            method2.isAccessible = true
            method2.invoke(delegate, minTime.minutes)

            val method3 = delegate.javaClass.getDeclaredMethod(
                "setIs24Hour", Boolean::class.javaPrimitiveType
            )
            method3.isAccessible = true
            method3.invoke(delegate, true)

            val method4 = delegate.javaClass.getDeclaredMethod(
                "setCurrentHour", Int::class.javaPrimitiveType
            )
            method4.isAccessible = true
            method4.invoke(delegate, maxTime.hours)

            val method5 = delegate.javaClass.getDeclaredMethod(
                "setCurrentMinute", Int::class.javaPrimitiveType
            )
            method5.isAccessible = true
            method5.invoke(delegate, maxTime.minutes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

}
