package com.example.kotlin_server_app

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.*

interface LogoutService {
    @FormUrlEncoded
    @POST("/members/gettoken/") // 아마 url인듯??
    fun requestlogout(
            // 여기가 인풋 정의하는 곳
            @Field("username") username:String?,
            @Field("password") password:String?
    ): Call<Logout> // 아웃풋을 정의하는 곳
}