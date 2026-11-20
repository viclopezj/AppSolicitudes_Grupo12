package com.example.appsolicitudes_grupo12.model.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Crea Retrofit una sola vez y entrega el ApiService listo para usar.
object RetrofitInstance {

    private const val URL_BASE = "https://dummyjson.com/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}