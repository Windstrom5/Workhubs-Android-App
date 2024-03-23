package com.windstrom5.tugasakhir.fragment

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.windstrom5.tugasakhir.activity.RegisterActivity
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
class AddLemburFragment : Fragment() {
    private lateinit var TINama: TextInputLayout
    private lateinit var TITanggal: TextInputLayout
    private lateinit var TIMasuk: TextInputLayout
    private lateinit var TIPulang: TextInputLayout
    private lateinit var TIPekerjaan: TextInputLayout
    private lateinit var uploadfileButton: Button
    private lateinit var selectedFileName: TextView
    private lateinit var changeFileButton: Button
    private lateinit var save: Button
    private var perusahaan : Perusahaan? = null
    private var pekerja : Pekerja? = null
    private lateinit var selectedFile: File
    private val PICK_IMAGE_REQUEST_CODE = 123
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_lembur, container, false)
        TINama = view.findViewById(R.id.nama)
        getBundle()
        TITanggal = view.findViewById(R.id.TITanggal)
        TIMasuk = view.findViewById(R.id.TIMasuk)
        TIPulang = view.findViewById(R.id.TIPulang)
        TIPekerjaan = view.findViewById(R.id.pekerjaan)
        uploadfileButton = view.findViewById(R.id.uploadfile)
        selectedFileName = view.findViewById(R.id.selectedFileName)
        changeFileButton = view.findViewById(R.id.changeFile)
        save = view.findViewById(R.id.cirsaveButton)
        TIMasuk.setEndIconOnClickListener{
            TIMasuk.editText?.let { it1 -> showDatePickerDialog(it1) }
        }
        TIPulang.setEndIconOnClickListener {
            TIPulang.editText?.let { it1 -> showDatePickerDialog(it1) }
        }
        TINama.editText?.addTextChangedListener(watcher)
        TITanggal.editText?.addTextChangedListener(watcher)
        TIMasuk.editText?.addTextChangedListener(watcher)
        TIPulang.editText?.addTextChangedListener(watcher)
        TIPekerjaan.editText?.addTextChangedListener(watcher)
        uploadfileButton.setOnClickListener {
            pickImageFromGallery()
        }

        // Set onClickListener for the save button
        save.setOnClickListener {
            // Implement logic to save the form data
            // You can access the entered data using the views like edNama.text.toString(), etc.
        }

        return view
    }

    private val watcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            // Not needed for this example
        }

        override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            // Not needed for this example
        }

        override fun afterTextChanged(editable: Editable?) {
            // Update the button state whenever a field is changed
            save.isEnabled = isAllFieldsFilled()
        }
    }
    private fun isAllFieldsFilled(): Boolean {
        return TINama.editText?.text?.isNotEmpty() ?: false &&
                TITanggal.editText?.text?.isNotEmpty()?: false  &&
                TIMasuk.editText?.text?.isNotEmpty() ?: false &&
                TIPulang.editText?.text?.isNotEmpty()?: false &&
                TIPekerjaan.editText?.text?.isNotEmpty()?: false &&
                selectedFileName.text != "No file selected"
    }
    private fun showDatePickerDialog(editText: EditText) {
        // Load holidays from JSON
        val holidaysMap = loadHolidaysFromJson(requireContext())

        val disabledDays = holidaysMap.keys.toTypedArray()

        val now = Calendar.getInstance()

        val dpd = DatePickerDialog.newInstance(
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }

                // Check if the selected date is a holiday
                if (holidaysMap.containsKey(selectedDate)) {
                    // Show Toast
                    Toast.makeText(
                        requireContext(),
                        "Selected date is a holiday. Please choose another date.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedDate.time)
                    editText.setText(formattedDate)
                }
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )

        dpd.setDisabledDays(disabledDays)
        dpd.show(childFragmentManager, "DatePickerDialog")
    }

    private fun loadHolidaysFromJson(context: Context): Map<Calendar, String> {
        val holidaysMap = mutableMapOf<Calendar, String>()

        try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.holidays)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            // Iterate through the keys (dates) in the JSON object
            for (key in jsonObject.keys()) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = Calendar.getInstance().apply {
                    time = dateFormat.parse(key) ?: Date()
                }
                val dateObject = jsonObject.getJSONObject(key)
                val summary = dateObject.getString("summary")

                // Add the date and summary to the map
                holidaysMap[date] = summary
                Log.d("A", "Holiday: Date = $key, Summary = $summary")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return holidaysMap
    }
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                // Get the real path from the URI
                val realPath = getRealPathFromUri(imageUri)
                if (realPath != null) {
                    selectedFileName.text = File(realPath).name
                    selectedFile = File(realPath)
                    selectedFileName.addTextChangedListener(watcher)
                } else {
                    selectedFileName.text = "Failed to get real path"
                }
            }
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(columnIndex)
        }
        return null
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
            TINama.editText?.setText(pekerja?.nama)
            Log.d("namaPekerja", pekerja?.nama.toString())
            TINama.isEnabled=false
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}
