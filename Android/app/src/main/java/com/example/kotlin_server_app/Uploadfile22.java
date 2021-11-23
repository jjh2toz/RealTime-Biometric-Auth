package com.example.kotlin_server_app;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface Uploadfile22 {
    @Multipart
    @GET("/authenticate/check_user/")
    Call<Upfile22> request(
            @Header("Authorization") String token,
            @Part MultipartBody.Part ECG
    );
}
