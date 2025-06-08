package dam.moviles.app_pricedetective.data.model

import com.google.gson.annotations.SerializedName

data class Amistad(
    @SerializedName("id_amistad") val id: Long,
    @SerializedName("id_usuario1") val usuario1: String,
    @SerializedName("id_usuario2") val usuario2: String,
    val estado: String,
    @SerializedName("fecha_solicitud") val fechaSolicitud: String,
    @SerializedName("fecha_actualizacion") val fechaActualizacion: String
)

// Envoltorio de respuesta para listar
data class ListarAmistadesResponse(
    val status: String,
    @SerializedName("amistades") val amistades: List<Amistad>
)

// Respuesta genérica (enviar solicitud, responder, eliminar)
data class ApiResponseAmigo(
    val status: String,
    val message: String
)