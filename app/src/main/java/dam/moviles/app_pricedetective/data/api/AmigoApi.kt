package dam.moviles.app_pricedetective.data.api

import dam.moviles.app_pricedetective.data.model.Amistad
import dam.moviles.app_pricedetective.data.model.ApiResponse
import dam.moviles.app_pricedetective.data.model.ApiResponseAmigo
import dam.moviles.app_pricedetective.data.model.ListarAmistadesResponse
import retrofit2.Response
import retrofit2.http.*

interface AmigoApi {
    @GET("amistades/list.php")
    suspend fun listarAmistades(
        @Query("id_usuario") idUsuario: String
    ): Response<ListarAmistadesResponse>

    @POST("amistades/sendRequest.php")
    suspend fun enviarSolicitud(
        @Body body: Map<String, String> // {"id_usuario1": "...", "id_usuario2": "..."}
    ): Response<ApiResponseAmigo>

    @PUT("amistades/respond.php")
    suspend fun responderSolicitud(
        @Body body: Map<String, String> // {"id_amistad": "123", "estado": "aceptada"}
    ): Response<ApiResponseAmigo>

    @HTTP(method = "DELETE", path = "amistades/eliminarAmigo.php", hasBody = true)
    suspend fun eliminarAmistad(
        @Body body: Map<String, String> // {"id_usuario1": "...", "id_usuario2": "..."}
    ): Response<ApiResponseAmigo>
}