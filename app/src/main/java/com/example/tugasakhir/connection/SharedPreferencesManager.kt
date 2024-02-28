package com.example.tugasakhir.connection

import android.content.Context
import android.content.SharedPreferences
import com.example.tugasakhir.model.Admin
import com.google.gson.Gson
import com.example.tugasakhir.model.login_session
import com.example.tugasakhir.model.Pekerja
import com.example.tugasakhir.model.Perusahaan

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    fun saveSession(login_session: login_session) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val loginJson = gson.toJson(login_session)
        editor.putString("login", loginJson)
        editor.apply()
    }

    fun getSession(): login_session? {
        val gson = Gson()
        val loginJson = sharedPreferences.getString("login", null)
        return if (loginJson != null) {
            gson.fromJson(loginJson, login_session::class.java)
        } else {
            null
        }
    }

    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.remove("pekerja")
        editor.remove("reviewer")
        editor.remove("login")
        editor.apply()
    }

    fun saveAdmin(admin: Admin) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val adminJson = gson.toJson(admin)
        editor.putString("admin", adminJson)
        editor.apply()
    }

    fun getAdmin(): Admin? {
        val gson = Gson()
        val adminJson = sharedPreferences.getString("admin", null)
        return if (adminJson != null) {
            gson.fromJson(adminJson, Admin::class.java)
        } else {
            null
        }
    }
    fun savePerusahaan(perusahaan: Perusahaan) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val perusahaanJson = gson.toJson(perusahaan)
        editor.putString("perusahaan", perusahaanJson)
        editor.apply()
    }

    fun getPerusahaan(): Perusahaan? {
        val gson = Gson()
        val perusahaanJson = sharedPreferences.getString("perusahaan", null)
        return if (perusahaanJson != null) {
            gson.fromJson(perusahaanJson, Perusahaan::class.java)
        } else {
            null
        }
    }
    fun savePekerja(pekerja: Pekerja) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val pekerjaJson = gson.toJson(pekerja)
        editor.putString("pekerja", pekerjaJson)
        editor.apply()
    }

    fun getPekerja(): Pekerja? {
        val gson = Gson()
        val adminJson = sharedPreferences.getString("pekerja", null)
        return if (adminJson != null) {
            gson.fromJson(adminJson, Pekerja::class.java)
        } else {
            null
        }
    }
}