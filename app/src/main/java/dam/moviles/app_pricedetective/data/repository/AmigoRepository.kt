package dam.moviles.app_pricedetective.data.repository

import dam.moviles.app_pricedetective.data.api.AmigoApi
import dam.moviles.app_pricedetective.data.model.Amistad
import dam.moviles.app_pricedetective.data.model.ListarAmistadesResponse
import dam.moviles.app_pricedetective.data.model.ApiResponseAmigo
import retrofit2.Response

class AmigoRepository(private val api: AmigoApi) {

    suspend fun getAmistades(idUsuario: String): List<Amistad> {
        android.util.Log.d("AmigoRepository", "Obteniendo amistades para usuario: $idUsuario")
        val resp: Response<ListarAmistadesResponse> = api.listarAmistades(idUsuario)
        android.util.Log.d("AmigoRepository", "Respuesta del servidor: ${resp.code()}")
        android.util.Log.d("AmigoRepository", "Cuerpo de la respuesta: ${resp.body()}")
        if (resp.isSuccessful && resp.body()?.status == "success") {
            val amistades = resp.body()!!.amistades
            android.util.Log.d("AmigoRepository", "Amistades encontradas: ${amistades.size}")
            return amistades
        }
        android.util.Log.e("AmigoRepository", "Error en la respuesta: ${resp.errorBody()?.string()}")
        throw Exception("Error al listar amistades: ${resp.errorBody()?.string()}")
    }

    suspend fun getAmigos(idUsuario: String): List<Amistad> {
        val amistades = getAmistades(idUsuario)
        val amigos = amistades.filter { it.estado == "aceptada" }
        android.util.Log.d("AmigoRepository", "Amigos aceptados encontrados: ${amigos.size}")
        return amigos
    }

    suspend fun getSolicitudesRecibidas(idUsuario: String): List<Amistad> =
        getAmistades(idUsuario).filter { it.estado == "pendiente" && it.usuario2 == idUsuario }

    suspend fun enviarSolicitud(idUsuario1: String, idUsuario2: String) {
        // Verificar si ya existe una relación
        val amistades = getAmistades(idUsuario1)
        val existeRelacion = amistades.any { 
            (it.usuario1 == idUsuario1 && it.usuario2 == idUsuario2) || 
            (it.usuario1 == idUsuario2 && it.usuario2 == idUsuario1)
        }
        
        if (existeRelacion) {
            throw Exception("Ya existe una solicitud o amistad entre estos usuarios")
        }

        val body = mapOf("id_usuario1" to idUsuario1, "id_usuario2" to idUsuario2)
        val resp: Response<ApiResponseAmigo> = api.enviarSolicitud(body)
        if (!resp.isSuccessful || resp.body()?.status != "success") {
            throw Exception("Error al enviar solicitud: ${resp.body()?.message ?: resp.errorBody()?.string()}")
        }
    }

    suspend fun responderSolicitud(idAmistad: Long, nuevoEstado: String) {
        val body = mapOf(
            "id_amistad" to idAmistad.toString(),
            "estado" to nuevoEstado
        )
        val resp: Response<ApiResponseAmigo> = api.responderSolicitud(body)
        if (!resp.isSuccessful || resp.body()?.status != "success") {
            throw Exception("Error al responder solicitud: ${resp.body()?.message ?: resp.errorBody()?.string()}")
        }
    }

    suspend fun eliminarAmistad(usuario1: String, usuario2: String) {
        val body = mapOf("id_usuario1" to usuario1, "id_usuario2" to usuario2)
        val resp: Response<ApiResponseAmigo> = api.eliminarAmistad(body)
        if (!resp.isSuccessful || resp.body()?.status != "success") {
            throw Exception("Error al eliminar amistad: ${resp.body()?.message ?: resp.errorBody()?.string()}")
        }
    }

    companion object {
        // Si quieres un singleton usando RetrofitClient:
        fun getInstance(): AmigoRepository =
            AmigoRepository(dam.moviles.app_pricedetective.data.api.RetrofitClient.amigoApi)
    }
}
