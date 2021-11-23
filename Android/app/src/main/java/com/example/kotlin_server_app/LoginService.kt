package com.example.kotlin_server_app
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

// 인터페이스 생성
interface LoginService{

    @FormUrlEncoded
    @POST("/members/login/") // url
    fun requestLogin(
        // 여기가 인풋 정의하는 곳
        @Field("username") username:String,
        @Field("password") password:String
    ): Call<Login> // 아웃풋을 정의하는 곳

}