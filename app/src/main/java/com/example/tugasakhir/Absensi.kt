package com.example.tugasakhir

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tugasakhir.databinding.ActivityAbsensiBinding

class Absensi : AppCompatActivity() {
    private lateinit var binding: ActivityAbsensiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}