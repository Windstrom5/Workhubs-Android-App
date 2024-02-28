package com.example.tugasakhir.model

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

class Pekerja (
    val id_perusahaan:Int,
    val email: String,
    val password: String,
    val nama: String,
    val tanggal_lahir: Date,
    val profile: ByteArray
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readSerializable() as Date,
        parcel.createByteArray() ?: byteArrayOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id_perusahaan)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(nama)
        parcel.writeSerializable(tanggal_lahir)
        parcel.writeByteArray(profile)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Pekerja> {
        override fun createFromParcel(parcel: Parcel): Pekerja {
            return Pekerja(parcel)
        }

        override fun newArray(size: Int): Array<Pekerja?> {
            return arrayOfNulls(size)
        }
    }
}