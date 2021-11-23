package com.example.kotlin_server_app
import android.icu.number.IntegerWidth
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// 회원가입
interface Newuser {
    @FormUrlEncoded
    @POST("/members/create/") // 아마 url인듯??

    fun requestLogin(
            // 여기가 인풋 정의하는 곳
            @Field("username") username:String,
            @Field("password") password:String,
            @Field("name") name:String
            //@Field("gender") gender:String,
            //@Field("age") age:Int,
            //@Field("gender") gender:String
    ): Call<New> // 아웃풋을 정의하는 곳
}