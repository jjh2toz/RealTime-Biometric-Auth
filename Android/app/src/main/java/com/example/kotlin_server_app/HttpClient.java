package com.example.kotlin_server_app;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class HttpClient {
    static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                if (original.url().encodedPath().equals("/members/login")
                        || original.url().encodedPath().equals("/members/create")){
                    chain.proceed(original);
                    return chain.proceed(original.newBuilder().build());
                } else {
                    return chain.proceed(original.newBuilder()
                            // 저장된 토큰으로 변경
                            .addHeader("Authorization", "Token b45ae5939298186aed28fe7446906f061417497f")
                            .build());
                }
            }).build();

    private static Retrofit retrofit;

    public static Retrofit getRetrofit(){
        if(retrofit == null){
            Retrofit.Builder builder = new Retrofit.Builder();
            builder.baseUrl("http://10.0.2.2:8000/");
            builder.client(client);
            builder.addConverterFactory(GsonConverterFactory.create());

            retrofit = builder.build();
        }

        return retrofit;
    }
}