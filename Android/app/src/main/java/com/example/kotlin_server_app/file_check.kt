package com.example.kotlin_server_app

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface file_check {
    @GET("/authenticate/get_model/")
    fun check(
            @Header("Authorization") user_token : String?
    ): Call<file_check_request>

}