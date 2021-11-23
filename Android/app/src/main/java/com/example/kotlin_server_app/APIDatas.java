package com.example.kotlin_server_app;

import com.google.gson.annotations.Expose;

/**
 * REQUEST
 */
class ReqLoginData {
    @Expose
    String username;
    String password;

    public ReqLoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

/**
 * RESPONSE
 */
// for isModel (각각 만들어야 할 듯)
class ResIsModel {
    @Expose
    Integer code;

    public Boolean isModel() {
        return code == 200;
    }
}

// token
class ResLoginData {
    @Expose
    String token;

    public String getToken() {
        return token;
    }
}