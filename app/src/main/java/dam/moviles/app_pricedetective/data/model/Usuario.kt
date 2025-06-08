package dam.moviles.app_pricedetective.data.model

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id_usuario") val idUsuario: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("foto_perfil") val fotoPerfil: String?,
    @SerializedName("fecha_registro") val fechaRegistro: String,
    @SerializedName("ultimo_acceso") val ultimoAcceso: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("rol") val rol: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password_hash") val password_hash: String
)

data class SignInRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("password_hash") val password_hash: String,
    @SerializedName("foto_perfil") val fotoPerfil: String?,
    @SerializedName("estado") val estado: String,
    @SerializedName("rol") val rol: String
)

data class ApiResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("user") val data: T?
) 