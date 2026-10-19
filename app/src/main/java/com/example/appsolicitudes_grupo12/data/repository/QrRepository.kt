package com.example.appsolicitudes_grupo12.data.repository

import com.example.appsolicitudes_grupo12.model.QrResult

class QrRepository {
    fun processQrContent(content: String): QrResult {
        // Aquí podrías guardar o procesar el QR en una BD o API
        return QrResult(content)
    }
}