package dam.moviles.app_pricedetective.data.api

import dam.moviles.app_pricedetective.data.model.ConfiguracionPrivacidad
import dam.moviles.app_pricedetective.data.model.ConfiguracionResponse
import retrofit2.Response
import retrofit2.http.*

interface ConfiguracionApi {
    @GET("configuracion/getSettings.php")
    suspend fun getConfiguracionPrivacidad(
        @Query("id_usuario") idUsuario: String
    ): Response<ConfiguracionResponse<ConfiguracionPrivacidad>>

    @PUT("configuracion/updateSettings.php")
    suspend fun updateConfiguracionPrivacidad(
        @Body configuracion: ConfiguracionPrivacidad
    ): Response<ConfiguracionResponse<Unit>>
} 