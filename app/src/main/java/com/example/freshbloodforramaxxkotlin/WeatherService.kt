package com.example.freshbloodforramaxxkotlin

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/forecast/hourly?")
    fun getCurrentWeatherData(@Query("appid") app_id: String, @Query("id") id : Int): Call<WeatherResp>
}
