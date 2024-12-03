package com.example.hairsuggestionipt;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("predict")
    Call<HairstyleResponse> getHairstyleSuggestion(@Body HairstyleRequest request);
}