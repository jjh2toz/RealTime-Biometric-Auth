package com.example.kotlin_server_app

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View

class custom_view(context: Context): View(context){
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event!!.action == MotionEvent.ACTION_DOWN){
            return false
        }
        return true;
    }
}