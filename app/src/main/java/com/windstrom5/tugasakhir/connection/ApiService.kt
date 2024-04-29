package com.windstrom5.tugasakhir.connection

import com.windstrom5.tugasakhir.model.Perusahaan
import com.windstrom5.tugasakhir.model.response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @Multipart
    @POST("DaftarPerusahaan")
    fun uploadPerusahaan(
        @Part("nama") nama: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("jam_masuk") jamMasuk: RequestBody,
        @Part("jam_keluar") jamKeluar: RequestBody,
        @Part("batas_aktif") batasAktif: RequestBody,
        @Part("secret_key") secretKey: RequestBody,
        @Part logo: MultipartBody.Part
    ): Call<ApiResponse>

    @Multipart
    @POST("DaftarAdmin")
    fun uploadAdmin(
        @Part("nama_perusahaan") nama_perusahaan: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("nama") nama: RequestBody,
        @Part("tanggal_lahir") tanggal_lahir: RequestBody,
        @Part profile: MultipartBody.Part
    ): Call<ApiResponse>

    @Multipart
    @POST("DaftarPekerja")
    fun uploadPekerja(
        @Part("nama_perusahaan") nama_perusahaan: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("nama") nama: RequestBody,
        @Part("tanggal_lahir") tanggal_lahir: RequestBody,
        @Part profile: MultipartBody.Part
    ): Call<ApiResponse>

    @Multipart
    @POST("AddLembur")
    fun uploadLembur(
        @Part("nama_perusahaan") nama_perusahaan: RequestBody,
        @Part("nama") nama: RequestBody,
        @Part("tanggal") tanggal: RequestBody,
        @Part("waktu_masuk") waktu_masuk: RequestBody,
        @Part("waktu_keluar") waktu_keluar: RequestBody,
        @Part("pekerjaan") pekerjaan: RequestBody,
        @Part bukti: MultipartBody.Part
    ): Call<ApiResponse>

    @Multipart
    @POST("AddDinas")
    fun uploadDinas(
        @Part("nama_perusahaan") nama_perusahaan: RequestBody,
        @Part("nama") nama: RequestBody,
        @Part("tujuan") tujuan: RequestBody,
        @Part("tanggal_berangkat") tanggal_berangkat: RequestBody,
        @Part("tanggal_pulang") tanggal_pulang: RequestBody,
        @Part("kegiatan") kegiatan: RequestBody,
        @Part bukti: MultipartBody.Part
    ): Call<ApiResponse>

    @Multipart
    @POST("AddIzin")
    fun uploadIzin(
        @Part("nama_perusahaan") nama_perusahaan: RequestBody,
        @Part("nama") nama: RequestBody,
        @Part("tanggal") tanggal: RequestBody,
        @Part("kategori") kategori: RequestBody,
        @Part("alasan") alasan: RequestBody,
        @Part bukti: MultipartBody.Part
    ): Call<ApiResponse>

    @GET("getLocation/{nama_perusahaan}")
    fun getLocationPekerja(
        @Path("nama_perusahaan") nama_perusahaan: String
    ): Call<ResponseBody>

    @GET("getAnggota/{nama_perusahaan}")
    fun getDataPekerja(
        @Path("nama_perusahaan") nama_perusahaan: String
    ): Call<ResponseBody>

    @GET("getDataLemburPerusahaan/{nama_perusahaan}")
    fun getDataLemburPerusahaan(
        @Path("nama_perusahaan") nama_perusahaan: String
    ): Call<ResponseBody>
    @GET("getDataLemburPekerja/{nama_perusahaan}/{nama_pekerja}")
    fun getDataLemburPekerja(
        @Path("nama_perusahaan") nama_perusahaan: String,
        @Path("nama_pekerja") nama_pekerja: String
    ): Call<ResponseBody>
    @GET("getDataDinasPerusahaan/{nama_perusahaan}")
    fun getDataDinasPerusahaan(
        @Path("nama_perusahaan") nama_perusahaan: String
    ): Call<ResponseBody>

    @GET("getDataDinasPekerja/{nama_perusahaan}/{nama_pekerja}")
    fun getDataDinasPekerja(
        @Path("nama_perusahaan") nama_perusahaan: String,
        @Path("nama_pekerja") nama_pekerja: String
    ): Call<ResponseBody>

    @GET("getDataIzinPerusahaan/{nama_perusahaan}")
    fun getDataIzinPerusahaan(
        @Path("nama_perusahaan") nama_perusahaan: String
    ): Call<ResponseBody>

    @GET("getDataIzinPekerja/{nama_perusahaan}/{nama_pekerja}")
    fun getDataIzinPekerja(
        @Path("nama_perusahaan") nama_perusahaan: String,
        @Path("nama_pekerja") nama_pekerja: String
    ): Call<ResponseBody>
    @PUT("UpdateStatusIzin")
    fun updatestatusIzin(
        @Part("id") id: RequestBody,
        @Part("status") status: RequestBody
    ): Call<ApiResponse>
    @PUT("UpdateStatusDinas")
    fun updatestatusDinas(
        @Part("id") id: RequestBody,
        @Part("status") status: RequestBody
    ): Call<ApiResponse>
    @PUT("UpdateStatusLembur")
    fun updatestatusLembur(
        @Part("id") id: RequestBody,
        @Part("status") status: RequestBody
    ): Call<ApiResponse>
    @Multipart
    @POST("UpdateDataIzin/{izinId}?_method=PUT")
    fun updateIzin(
        @Path("izinId") izinId: Int,
        @Part("tanggal") tanggal: RequestBody,
        @Part("kategori") kategori: RequestBody,
        @Part("alasan") alasan: RequestBody,
        @Part bukti: MultipartBody.Part
    ): Call<ApiResponse>
    @Multipart
    @POST("UpdateDataLembur/{dinasId}?_method=PUT")
    fun updateLembur(
        @Path("lemburId") lemburId: Int,
        @Part("tanggal") tanggal: RequestBody,
        @Part("masuk") masuk: RequestBody,
        @Part("pulang") pulang: RequestBody,
        @Part("pekerjaan") pekerjaan: RequestBody,
        @Part bukti: MultipartBody.Part
    ): Call<ApiResponse>
    @Multipart
    @POST("UpdateDataDinas/{dinasId}?_method=PUT")
    fun updateDinas(
        @Path("dinasId") dinasId: Int,
        @Part("berangkat") berangkat: RequestBody,
        @Part("pulang") pulang: RequestBody,
        @Part("tujuan") tujuan: RequestBody,
        @Part("kegiatan") kegiatan: RequestBody,
        @Part bukti: MultipartBody.Part
    ): Call<ApiResponse>
    @DELETE("DeletePerusahaan/{id}")
    fun deleteCompany(@Path("id") id: Int): Call<Void>

    @GET("GetPerusahaan")
    fun getPerusahaan():Call<ResponseBody>
    @GET("perusahaan/{nama_perusahaan}")
    fun getPerusahaan(@Path("nama_perusahaan") namaPerusahaan: String): Call<Perusahaan>
    @GET("checkEmail") // Adjust the endpoint accordingly
    fun checkEmail(@Query("email") email: String): Call<response>
    @Multipart
    @POST("resetPassword")
    fun resetPassword(
        @Path("email") email: String,
    ): Call<ApiResponse>
}

