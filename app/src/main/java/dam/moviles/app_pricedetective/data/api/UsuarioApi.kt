package dam.moviles.app_pricedetective.data.api

import dam.moviles.app_pricedetective.data.model.ApiResponse
import dam.moviles.app_pricedetective.data.model.LoginRequest
import dam.moviles.app_pricedetective.data.model.SignInRequest
import dam.moviles.app_pricedetective.data.model.Usuario
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface UsuarioApi {
    @POST("usuarios/logIn.php")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ApiResponse<Usuario>>

    @POST("usuarios/signIn.php")
    suspend fun signIn(@Body signInRequest: SignInRequest): Response<ApiResponse<Usuario>>

    @GET("usuarios/perfil.php")
    suspend fun getPerfil(@Query("id_usuario") idUsuario: String): Response<ApiResponse<Usuario>>

    @GET("usuarios/mostrarTodos.php")
    suspend fun getAllUsuarios(): Response<List<Usuario>>

    @PUT("usuarios/updatePerfil.php")
    suspend fun updatePerfil(@Body usuario: Usuario): Response<ApiResponse<Usuario>>

    @PUT("usuarios/updateEstado.php")
    suspend fun updateEstadoUsuario(
        @Body request: Map<String, String>
    ): Response<ApiResponse<Boolean>>

    @GET("usuarios/buscar.php")
    suspend fun buscarUsuarios(@Query("query") query: String): Response<List<Usuario>>
} 