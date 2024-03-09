package com.windstrom5.tugasakhir.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class Absen(
    val id: Int? = null,
    val idPekerja: Int,
    val idPerusahaan: Int,
    val tanggal: Date,
    val jamMasuk: String, // Change to your preferred time representation (e.g., String)
    val jamKeluar: String, // Change to your preferred time representation (e.g., String)
    val latitude: Double,
    val longitude: Double,
    val updatedAt: Date
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readInt(),
        parcel.readInt(),
        Date(parcel.readLong()),
        parcel.readString() ?: "", // Change to your preferred time representation (e.g., String)
        parcel.readString() ?: "", // Change to your preferred time representation (e.g., String)
        parcel.readDouble(),
        parcel.readDouble(),
        Date(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeInt(idPekerja)
        parcel.writeInt(idPerusahaan)
        parcel.writeLong(tanggal.time)
        parcel.writeString(jamMasuk)
        parcel.writeString(jamKeluar)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeLong(updatedAt.time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Absen> {
        override fun createFromParcel(parcel: Parcel): Absen {
            return Absen(parcel)
        }

        override fun newArray(size: Int): Array<Absen?> {
            return arrayOfNulls(size)
        }
    }
}
