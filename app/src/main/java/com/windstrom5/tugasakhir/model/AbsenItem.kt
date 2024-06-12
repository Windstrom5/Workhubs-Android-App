package com.windstrom5.tugasakhir.model

import java.sql.Time
import java.util.Date

data class AbsenItem(
    val id: Int? = null,
    val nama_pekerja: String,
    val nama_perusahaan: String,
    val tanggal: Date,
    val masuk: Time,
    val keluar: Time? = null
)