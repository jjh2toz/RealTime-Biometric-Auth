package com.example.kotlin_server_app

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.editTextTextPassword
import kotlinx.android.synthetic.main.activity_main.editTextTextPersonName
import kotlinx.android.synthetic.main.new_user_page.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Newperson : AppCompatActivity(){ // 회원가입 페이지

    //http://223.194.46.83:25900
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_user_page)

        val clientBuilder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(loggingInterceptor)

        var retrofit = Retrofit.Builder()
                .baseUrl("http://223.194.46.83:25900")
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build()


        var newuserservice = retrofit.create(Newuser::class.java)


       registerbutton.setOnClickListener {
           val intent = Intent(this,MainActivity::class.java) // 회원가입 후 로그인 페이지로 이동

           var newname = editTextTextname.text.toString()
           var newId = editTextTextPersonName.text.toString()
           var newPw = editTextTextPassword.text.toString()
           //var newgender = editTextTextgender.text.toString()
           //var buff = editTextTextage.text.toString()
           //var newage = Integer.parseInt(buff)

           newuserservice.requestLogin(newId,newPw,newname).enqueue(object: Callback<New>{
               override fun onFailure(call: Call<New>, t: Throwable) {
                   // 웹 통신에 실패시 실행
                   var dialog = AlertDialog.Builder(this@Newperson)
                   dialog.setTitle("실패!")
                   dialog.setMessage("통신에 실패....")
                   dialog.show()
               }
               override fun onResponse(call: Call<New>, response: Response<New>) {
                   var NewU = response.body() // msuccess
                   println("<<<<<<<<<<<<<<<<<<<<<############################3"+NewU?.code)
                   var dialog = AlertDialog.Builder(this@Newperson)
                   if(NewU?.code != 200) {
                       dialog.setTitle("경고!")
                       dialog.setMessage("이미 존재하는 계정 입니다.")
                       dialog.show()

                       editTextTextname.setText("")
                       editTextTextPersonName.setText("")
                       editTextTextPassword.setText("")
                   }
                   else
                   {
                       Toast.makeText(this@Newperson, "회원가입 성공", Toast.LENGTH_SHORT).show()
                       startActivity(intent)
                       finish()
                   }
               }
           })
       }
    }
}