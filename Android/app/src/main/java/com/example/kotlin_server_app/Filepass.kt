package com.example.kotlin_server_app

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_loginafter.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class Filepass : AppCompatActivity() {

    val TAG:String = "Filepass"

    fun showPrograss(isShow:Boolean) {
        if (isShow) loading_linear_a.visibility = View.VISIBLE
        else loading_linear_a.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginafter)

        showPrograss(false)

        val ecg = findViewById<View>(R.id.iv_authentication_gif) as ImageView
        Glide.with(this).asGif().load(R.raw.heart_rate).into(ecg)

        val sharedPreferences = getSharedPreferences("auto_token", 0) // 자동 로그인 토큰 파일
        val editor = sharedPreferences.edit()
        val token_check = sharedPreferences.getString("token", null)
        val token_s = sharedPreferences.getString("ustoken",null)

        val checktoken = "Token "+token_s
        val checktoken2 = "Token "+token_check

        //intent명
        val ten_seconds = Intent(this,BluetoothActivity::class.java) // 10초 측정
        val ten_minutes = Intent(this,BluetoothActivity2::class.java) // measure2로 이동 (10분 측정)

        println("????????????????????????????????????/"+token_s)

        if (token_check != null) { //자동로그인으로 들어온경우
            val makemodel = Intent(this,Filepass_newuser::class.java)


            filepassbutton1.setOnClickListener { // 파일 전송 버튼 // 인증 버튼

                showPrograss(true)

                val clientBuilder = OkHttpClient.Builder()
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                clientBuilder.addInterceptor(loggingInterceptor)

                val okHttpClient = OkHttpClient.Builder() // 모델 확인 하는데 걸리는 시간에 timeout 설정 해두기 (오래 걸리기 때문)
                        .connectTimeout(10, TimeUnit.MINUTES)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()

                var retrofit = Retrofit.Builder()
                        .baseUrl("http://223.194.46.83:25900")
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                var file_message = AlertDialog.Builder(this@Filepass)
                var filecheckservice = retrofit.create(file_check::class.java)

                // 파일 존재 확인 // 토큰으로 가능
                filecheckservice.check(checktoken2).enqueue(object : Callback<file_check_request> {
                    override fun onResponse(call: Call<file_check_request>, response: Response<file_check_request>) {
                        if(response.body()?.code == 200) // 파일이 존재한다면
                        {
                            println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF 자동 로그인 코드 : "+response.body()?.code)
                            startActivity(ten_seconds) // 10초 측정하는 곳으로 이동

                        }
                        else if(response.body()?.code == 400)
                        {
                            file_message.setTitle("파일이 존재하지 않음 - 모델 생성 진행 하시겠습니까?")
                            file_message.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                                startActivity(ten_minutes) // 10분측정 페이지로 이동 // measure2로 이동
                            })
                            file_message.setNegativeButton("아니오",DialogInterface.OnClickListener { dialog, which->
                            })
                            file_message.show()
                        }
                        else
                        {
                            println("))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))"+response.body()?.code)
                            file_message.setTitle("걍 오류")
                            file_message.show()
                            //makemodel.putExtra("ustoken",token_check) // 토큰 전송
                            //startActivity(makemodel) // 모델 생성 페이지 이동
                        }
                    }
                    override fun onFailure(call: Call<file_check_request>, t: Throwable) {
                        file_message.setTitle("파일 찾기 실패!")
                        file_message.show()
                    }
                })

            }
        }
        else { // 로그인 후 들어온 경우 ( 자동로그인 x )

            val makemodels = Intent(this, Filepass_newuser::class.java)

            filepassbutton1.setOnClickListener { // 파일 전송 버튼 // 인증 버튼

                showPrograss(true)

                val clientBuilder = OkHttpClient.Builder()
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                clientBuilder.addInterceptor(loggingInterceptor)

                val okHttpClient = OkHttpClient.Builder() // 모델 확인 하는데 걸리는 시간에 timeout 설정 해두기 (오래 걸리기 때문)
                        .connectTimeout(10, TimeUnit.MINUTES)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()

                var retrofit = Retrofit.Builder()
                        .baseUrl("http://223.194.46.83:25900")
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                var file_message = AlertDialog.Builder(this@Filepass)
                var filecheckservice = retrofit.create(file_check::class.java)

                // 파일 존재 확인
                filecheckservice.check(checktoken).enqueue(object : Callback<file_check_request> {
                    override fun onResponse(call: Call<file_check_request>, response: Response<file_check_request>) {
                        if (response.body()?.code == 200) { // 파일 존재
                            println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF 일반 로그인 코드 : "+response.body()?.code)
                            startActivity(ten_seconds) // 10초 측정하는 곳으로 이동

                        } else if (response.body()?.code == 400) {
                            showPrograss(false)
                            file_message.setTitle("파일이 존재하지 않음 - 모델 생성 진행 하시겠습니까?")
                            file_message.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                                startActivity(ten_minutes) // 10분 측정하느 곳으로 이동
                            })
                            file_message.setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, which ->
                            })
                            file_message.show()

                        } else {
                            file_message.setTitle("걍 오류")
                            file_message.show()
                            makemodels.putExtra("ustoken", token_s) // 토큰 전송
                            startActivity(makemodels) // 모델 생성 페이지 이동
                            finish()
                        }
                    }
                    override fun onFailure(call: Call<file_check_request>, t: Throwable) {
                        file_message.setTitle("파일 찾기 실패!")
                        file_message.show()
                        finish()
                    }
                })

            }
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}