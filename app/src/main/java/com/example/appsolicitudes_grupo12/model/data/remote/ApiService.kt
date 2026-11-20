package com.example.appsolicitudes_grupo12.model.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// Llamadas a la API del catálogo. Retrofit genera el código a partir de esta interfaz.
interface ApiService {

    // GET https://dummyjson.com/products?limit=...&select=...
    // "select" pide solo esos campos (el id siempre viene).
    @GET("products")
    suspend fun obtenerProductos(
        @Query("limit") limite: Int,
        @Query("select") campos: String
    ): ProductosRespuesta
}