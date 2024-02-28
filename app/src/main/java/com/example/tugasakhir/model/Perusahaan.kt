package com.example.tugasakhir.model

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class Perusahaan(
    val nama: String,
    val latitude: Double,
    val longitude: Double,
    val batasAktif: String,
    val logo: ByteArray,
    val secret_key: String,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.createByteArray() ?: byteArrayOf(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nama)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(batasAktif)
        parcel.writeByteArray(logo)
        parcel.writeString(secret_key)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Perusahaan> {
        override fun createFromParcel(parcel: Parcel): Perusahaan {
            return Perusahaan(parcel)
        }

        override fun newArray(size: Int): Array<Perusahaan?> {
            return arrayOfNulls(size)
        }
    }
}