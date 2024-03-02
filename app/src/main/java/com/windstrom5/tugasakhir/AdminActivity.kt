package com.windstrom5.tugasakhir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.windstrom5.tugasakhir.databinding.ActivityAdminBinding
import java.util.Calendar

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var tv : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val text = "© $currentYear Your Company Name. /nAll rights reserved."
        tv = binding.courtesyNoticeTextView
        tv.setText(text)
    }
}