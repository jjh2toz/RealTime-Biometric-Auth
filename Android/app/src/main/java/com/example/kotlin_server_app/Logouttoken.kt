package com.example.kotlin_server_app

import retrofit2.Call
import retrofit2.http.*

interface Logouttoken {
    @GET("/members/logout/")
    fun logout(
            @Header("Authorization") user_token : String?
    ): Call<Logoutt>
}