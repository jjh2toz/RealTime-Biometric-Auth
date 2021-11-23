package com.example.kotlin_server_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceStare) {

        super.onCreate(savedInstanceStare);
        setContentView(R.layout.activity_splash);

        ImageView ecg = (ImageView) findViewById(R.id.splash_gif);
        Glide.with(this).asGif().load(R.raw.heart_rate).into(ecg);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }

}
