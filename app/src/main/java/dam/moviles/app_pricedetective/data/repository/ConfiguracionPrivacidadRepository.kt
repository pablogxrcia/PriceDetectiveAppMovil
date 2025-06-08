package dam.moviles.app_pricedetective.data.repository

import android.util.Log
import dam.moviles.app_pricedetective.data.api.RetrofitClient
import dam.moviles.app_pricedetective.data.model.ConfiguracionPrivacidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfiguracionPrivacidadRepository {
    private val api = RetrofitClient.configuracionApi
    private val TAG = "ConfiguracionRepository"

    suspend fun getConfiguracionPrivacidad(idUsuario: String): Result<ConfiguracionPrivacidad> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo configuración para usuario: $idUsuario")
            val response = api.getConfiguracionPrivacidad(idUsuario)
            
            Log.d(TAG, "Código de respuesta: ${response.code()}")
            Log.d(TAG, "Respuesta completa: ${response.body()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(TAG, "Status: ${responseBody?.status}")
                Log.d(TAG, "Message: ${responseBody?.message}")
                Log.d(TAG, "Data: ${responseBody?.data}")

                if (responseBody?.status == "success") {
                    val configuracion = responseBody.data
                    if (configuracion != null) {
                        Log.d(TAG, "Configuración obtenida exitosamente: $configuracion")
                        Result.success(configuracion)
                    } else {
                        // La API devolvió success pero data es null, lo que significa que no existe configuración
                        Log.d(TAG, "No existe configuración para el usuario, creando una por defecto")
                        val defaultConfig = ConfiguracionPrivacidad(
                            idUsuario = idUsuario,
                            mostrarUltimaConexion = 1,
                            mostrarEstado = 1
                        )
                        // Intentar guardar la configuración por defecto
                        val updateResult = updateConfiguracionPrivacidad(defaultConfig)
                        if (updateResult.isSuccess) {
                            Result.success(defaultConfig)
                        } else {
                            Result.failure(Exception("Error al crear configuración por defecto"))
                        }
                    }
                } else {
                    val errorMessage = responseBody?.message ?: "Error al obtener la configuración"
                    Log.e(TAG, "Error en la respuesta: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error en la respuesta HTTP: $errorBody")
                Result.failure(Exception("Error en la respuesta HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener configuración: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateConfiguracionPrivacidad(configuracion: ConfiguracionPrivacidad): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Actualizando configuración: $configuracion")
            val response = api.updateConfiguracionPrivacidad(configuracion)
            
            Log.d(TAG, "Código de respuesta: ${response.code()}")
            Log.d(TAG, "Respuesta completa: ${response.body()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(TAG, "Status: ${responseBody?.status}")
                Log.d(TAG, "Message: ${responseBody?.message}")

                if (responseBody?.status == "success") {
                    Log.d(TAG, "Configuración actualizada exitosamente")
                    Result.success(Unit)
                } else {
                    val errorMessage = responseBody?.message ?: "Error al actualizar la configuración"
                    Log.e(TAG, "Error en la respuesta: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error en la respuesta HTTP: $errorBody")
                Result.failure(Exception("Error en la respuesta HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar configuración: ${e.message}", e)
            Result.failure(e)
        }
    }
} 