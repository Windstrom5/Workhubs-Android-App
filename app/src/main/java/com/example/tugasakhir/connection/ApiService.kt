package com.example.tugasakhir.connection

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("upload") // Replace with your actual endpoint
    fun uploadData(
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("tanggal") tanggal: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>
}
