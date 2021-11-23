package com.example.kotlin_server_app

//import androidx.appcompat.app.AlertDialog
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Thread.sleep


class MainActivity : AppCompatActivity() {
    //val GOOGLE_REQUEST_CODE = 99
    val TAG = "<<<<<<<<<<<<<<LoginPage>>>>>>>>>>>>"

    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    fun showPrograss(isShow:Boolean) {
        if (isShow) loading_linear.visibility = View.VISIBLE
        else loading_linear.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("auto_token", 0) // 자동 로그인 파일
        val editor = sharedPreferences.edit()

        val token_check = sharedPreferences.getString("token", null)
        val authentication_page = Intent(this, Filepass::class.java)
        //val bluetooth_active = Intent(this,BluetoothActivity::class.java) // 모듈 선택창 intent

        //var api: ApiInterface
        showPrograss(false)

        // 자동로그인
        if (token_check != null) {
            startActivity(authentication_page); // 토큰 존재시 인증페이지 (블루투스 10초)
        }

        // retrofit 객체 생성
        var retrofit = Retrofit.Builder()
                .baseUrl("http://223.194.46.83:25900")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        // retrofit 객체에서 create를 통해서 서비스를 올려줌
        var loginService = retrofit.create(LoginService::class.java)

        // 회원가입 버튼
        new_user_button.setOnClickListener {
            val intent2 = Intent(this, Newperson::class.java)
            startActivity(intent2) // 회원 가입 페이지로 이동
            finish()
        }
        // 로그인 버튼
        button.setOnClickListener {

            showPrograss(true)

            //val intent = Intent(this, Filepass::class.java)

            // 로그인 버튼 클릭 시 다음 화면으로 넘김
            val loginkeep = Intent(this, MainActivity::class.java)

            var textId = editTextTextPersonName.text.toString()
            var textPw = editTextTextPassword.text.toString()


            // 로그인 (서버와 통신)
            loginService.requestLogin(textId, textPw).enqueue(object : Callback<Login> {
                override fun onFailure(call: Call<Login>, t: Throwable) {
                    // 웹 통신에 실패시 실행
                    var dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("실패!")
                    dialog.setMessage("통신에 실패....")
                    dialog.show()
                }

                override fun onResponse(call: Call<Login>, response: Response<Login>) {
                    // 성공시 응답값을 받아옴
                    var login = response.body() // Login의 token
                    println("#################################################login_check : " + login?.token + "#################################################")

                    if (login?.token == null) {
                        var file_messages = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                        file_messages.setTitle("아이디 또는 비밀번호가 틀렸습니다.")
                        file_messages.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                        })
                        file_messages.show()
                        editTextTextPersonName.setText("")
                        editTextTextPassword.setText("")
                        showPrograss(false)

                    } else{
                        println("((((((((((((((((((((((((((((((((((((((("+login?.token)
                        //////////////// 토큰 저장 /////////////////////////////
                        editor.putString("ustoken",login?.token) // 유저의 토큰 파일로 저장
                        editor.apply()
                        Log.e(TAG, "쉐어드에 저장된 ustoken = " + sharedPreferences.getString("ustoken", ""))
                        ///////////////////////////////////////////////////////


                        if (checkboxbutton.isChecked) { // 자동 로그인 버튼 클릭 되어있으면
                            editor.putString("token", login?.token) // 자동 로그인 토큰 저장
                            //editor.putString("username",textId) // 아이디도 저장
                            editor.apply()
                            Log.e(TAG, "쉐어드에 저장된 token = " + sharedPreferences.getString("token", ""))
                        }

                        showPrograss(false)
                        startActivity(authentication_page) // 인증 이동
                        finish()
                    }
                }
            })

        }

    }

}