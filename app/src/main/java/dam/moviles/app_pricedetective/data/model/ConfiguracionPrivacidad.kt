package dam.moviles.app_pricedetective.data.model

import com.google.gson.annotations.SerializedName

data class ConfiguracionPrivacidad(
    @SerializedName("id_usuario")
    val idUsuario: String,
    @SerializedName("mostrar_ultima_conexion")
    val mostrarUltimaConexion: Int,
    @SerializedName("mostrar_estado")
    val mostrarEstado: Int
) {
    // Propiedades computadas para convertir Int a Boolean
    val mostrarUltimaConexionBoolean: Boolean
        get() = mostrarUltimaConexion == 1

    val mostrarEstadoBoolean: Boolean
        get() = mostrarEstado == 1
}

data class ConfiguracionResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
) 