package com.example.conferenceproject.Model.Api;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiService {
    @POST("send")
    Call<String> sendRemoteMessage(
            @HeaderMap HashMap<String, String> headers ,
            @Body String remoteBody
    );
}
