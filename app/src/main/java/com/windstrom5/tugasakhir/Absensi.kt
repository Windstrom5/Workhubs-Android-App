package com.windstrom5.tugasakhir

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.windstrom5.tugasakhir.databinding.ActivityAbsensiBinding

class Absensi : AppCompatActivity() {
    private lateinit var binding: ActivityAbsensiBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}