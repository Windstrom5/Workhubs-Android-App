package com.example.tugasakhir.model

import java.io.File
import java.util.Date

data class Admin(
    val email: String,
    val password: String,
    val nama: String,
    val tanggal_lahir: Date,
    val profile: ByteArray
)