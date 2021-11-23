package com.example.kotlin_server_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlin_server_app.MeasureActivity.retrofit
import com.github.mikephil.charting.data.LineData
import kotlinx.android.synthetic.main.activity_loginafter.loading_linear_a
import kotlinx.android.synthetic.main.loading_page.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class Authfin : AppCompatActivity() { // 측정 후 인증

    val TAG:String = "Auth_fin"

//    fun showPrograss(isShow:Boolean) {
//        if (isShow) loading_linear_a.visibility = View.VISIBLE
//        else loading_linear_a.visibility = View.GONE
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading_page)

        //showPrograss(false)

        val intent2 = Intent(this,ResultfailActivity::class.java)
        val intent = Intent(this,ResultActivity::class.java)

        // chart

        // chart
//        val line_data = LineData()
//        chart.setBackgroundColor(Color.rgb(250, 250, 250))
//        chart.description = null
//        chart.axisLeft.setDrawGridLines(false)
//        chart.xAxis.setDrawGridLines(false)
//        chart.axisLeft.setDrawLabels(false)
//        chart.axisRight.setDrawLabels(false)
//        chart.xAxis.setDrawLabels(false)
//        chart.legend.isEnabled = false // Hide the legend
//
//        val lyl = chart.axisLeft
//
//        // 보여질 위치
//
//        // 보여질 위치
//        lyl.axisMaximum = 4f
//        lyl.axisMinimum = -4f



        // 저장된 토큰 가져오기
        val sharedPreferences = getSharedPreferences("auto_token", 0) // 자동 로그인 토큰 파일
        val token_check = sharedPreferences.getString("token", null)
        val token_s = sharedPreferences.getString("ustoken",null)
        val checktoken = "Token "+token_s

        var fileuploadservice = retrofit.create(Uploadfile::class.java)
        val file2 = File("/data/user/0/com.example.kotlin_server_app/files/ECG.csv")
        val requestFile = RequestBody.create("*/*".toMediaTypeOrNull(), file2)
        //val requestFile1 = RequestBody.create("text/plain".toMediaTypeOrNull(), checktoken.toString())
        val body1 = MultipartBody.Part.createFormData("ECG", file2.name, requestFile)


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

        var fileupload = retrofit.create(Uploadfile::class.java)

        println("*****************************************************************************")
        auth_button_ok.setOnClickListener {
            //showPrograss(true)
            fileupload.request(checktoken, body1).enqueue(object : Callback<Upfile> {
                override fun onResponse(call: Call<Upfile>, response: Response<Upfile>) {
                    var dialog = AlertDialog.Builder(this@Authfin)
                    println("result <<<<<<<<<<<<<<<<<<<<<<<<" + response.body()?.code)
                    if (response.body()?.code == 200) { // 인증 성공
                        //showPrograss(false)
                        startActivity(intent) // 인증 성공시 성공 페이지로 이동
                        finish()
                    } else {
                        println("인증 실패")
                        //showPrograss(false)
                        startActivity(intent2) // 인증 실패시 실패 페이지로 이동
                        finish()
                    }
                }

                override fun onFailure(call: Call<Upfile>, t: Throwable) {
                    var dialog = AlertDialog.Builder(this@Authfin)
                    dialog.setTitle("전송실패!")
                    dialog.setMessage("다시 시도 하시오")
                    dialog.show()

                }
            })
        }
    }
}