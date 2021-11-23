package com.example.kotlin_server_app

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.SystemClock.sleep
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_loginafter.*
import kotlinx.android.synthetic.main.activity_loginafter.filepassbutton1
import kotlinx.android.synthetic.main.activity_loginnew_after.*
//import kotlinx.android.synthetic.main.activity_loginnew_after.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.loading_linear
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileInputStream
import okhttp3.*
import java.util.concurrent.TimeUnit


class Filepass_newuser : AppCompatActivity() {
    private lateinit var sharedPreferencess : SharedPreferences
    private lateinit var editors : SharedPreferences.Editor

    fun showPrograss(isShow:Boolean) {
        if (isShow) loading_linear.visibility = View.VISIBLE
        else loading_linear.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginnew_after)

        val ecg = findViewById<View>(R.id.iv_authentication_gif) as ImageView
        Glide.with(this).asGif().load(R.raw.heart_rate).into(ecg)

        // 로그인시 저장된 토큰 가져오기
        val sharedPreferences = getSharedPreferences("auto_token", 0) // 저장된 토큰 파일
        val editor2 = sharedPreferences.edit()
        val token_s = sharedPreferences.getString("ustoken", null)
        println("모델 생성 페이지 토큰 : <<<<<<<<<<<<<<<<<<<<<<<<<<<<"+ token_s)

        val clientBuilder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(loggingInterceptor)

        val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

        showPrograss(false)
        var retrofit = Retrofit.Builder()
                .baseUrl("http://223.194.46.83:25900")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            filepassbutton1.setOnClickListener { // 파일 전송 버튼
                // intent 종류
                val auth_page = Intent(this,Filepass::class.java) // 인증 페이지로 이동

                showPrograss(true)

                val file2 = java.io.File("/data/user/0/com.example.kotlin_server_app/files/BSW_200629_new.mat")
                val requestFile = RequestBody.create("*/*".toMediaTypeOrNull(), file2)
                //val requestFile1 = RequestBody.create("text/plain".toMediaTypeOrNull(), token_s.toString()) // 형변환
                // 일단 multipart 사용하기 위해서 requestbody로 변환
                val body1 = MultipartBody.Part.createFormData("EEG", file2.name, requestFile)

                sleep(2000)
                //모델 생성되었다고 가정
                Toast.makeText(this@Filepass_newuser, "모델 생성 성공", Toast.LENGTH_SHORT).show() // toast 메시지

                showPrograss(false)
                //startActivity(auth_page)
                finish()

            }
        }

    override fun onPause() {
        super.onPause()
        finish()
    }
}



