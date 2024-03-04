package com.windstrom5.tugasakhir.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.windstrom5.tugasakhir.databinding.ActivityUserBinding
import java.util.Calendar

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    private lateinit var tv : TextView
    private lateinit var namaPerusahaan : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val text = "© $currentYear $namaPerusahaan. /nAll rights reserved."
        tv = binding.courtesyNoticeTextView
        tv.setText(text)
    }
}