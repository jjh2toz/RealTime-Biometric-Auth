package com.example.kotlin_server_app

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface Uploadfile_new {
    @Multipart
    @POST("/authenticate/make_model/")
    fun request(
            @Header("Authorization") token : String?,
            @Part EEG: MultipartBody.Part?
    ): Call<Upfile>
}
