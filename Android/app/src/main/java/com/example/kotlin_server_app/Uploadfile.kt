package com.example.kotlin_server_app

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface Uploadfile {
        @Multipart
        @POST("/authenticate/check_user/")
        fun request(
                //@Header("Authorization") user_token : String?,
                //@Part ("token") token: RequestBody,
                @Header("Authorization") token : String,
                @Part ECG : MultipartBody.Part?
        ): Call<Upfile>
}
