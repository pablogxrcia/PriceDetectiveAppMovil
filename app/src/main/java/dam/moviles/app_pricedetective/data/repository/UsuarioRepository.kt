package dam.moviles.app_pricedetective.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dam.moviles.app_pricedetective.data.api.RetrofitClient
import dam.moviles.app_pricedetective.data.model.ApiResponse
import dam.moviles.app_pricedetective.data.model.LoginRequest
import dam.moviles.app_pricedetective.data.model.SignInRequest
import dam.moviles.app_pricedetective.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UsuarioRepository {
    private val api = RetrofitClient.usuarioApi
    private val TAG = "UsuarioRepository"
    private val gson = Gson()

    suspend fun login(email: String, password: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== INICIO LOGIN ===")
            Log.d(TAG, "Email recibido: '$email'")
            Log.d(TAG, "Password recibido: '$password'")

            if (email.isBlank() || password.isBlank()) {
                Log.e(TAG, "Email o contraseña vacíos")
                return@withContext Result.failure(Exception("Por favor, completa todos los campos"))
            }

            val loginRequest = LoginRequest(
                email = email.trim(),
                password_hash = password.trim()
            )

            Log.d(TAG, "LoginRequest creado: $loginRequest")
            Log.d(TAG, "URL de la petición: ${RetrofitClient.BASE_URL}usuarios/logIn.php")

            val response = api.login(loginRequest)
            Log.d(TAG, "Código de respuesta: ${response.code()}")
            
            if (response.isSuccessful && response.body()?.status == "success") {
                val usuario = response.body()?.data
                if (usuario != null) {
                    Log.d(TAG, "Login exitoso para usuario: ${usuario.username}")
                    return@withContext Result.success(usuario)
                }
            }
            
            val errorMessage = response.body()?.message ?: "Error en el login"
            Log.e(TAG, "Error en login: $errorMessage")
            return@withContext Result.failure(Exception(errorMessage))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}")
            return@withContext Result.failure(e)
        }
    }

    suspend fun signIn(
        nombre: String,
        email: String,
        username: String,
        password: String,
        fotoPerfil: String? = null
    ): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== INICIO REGISTRO ===")
            Log.d(TAG, "Datos recibidos: nombre='$nombre', email='$email', username='$username', fotoPerfil='$fotoPerfil'")

            // Validar campos obligatorios
            if (nombre.isBlank() || email.isBlank() || username.isBlank() || password.isBlank()) {
                Log.e(TAG, "Campos obligatorios vacíos")
                return@withContext Result.failure(Exception("Por favor, completa todos los campos obligatorios"))
            }

            val request = SignInRequest(
                nombre = nombre.trim(),
                email = email.trim(),
                username = username.trim(),
                password_hash = password.trim(),
                fotoPerfil = fotoPerfil,
                estado = "activo",
                rol = "usuario"
            )

            Log.d(TAG, "SignInRequest creado: $request")
            Log.d(TAG, "URL de la petición: ${RetrofitClient.BASE_URL}usuarios/signIn.php")

            try {
                val response = api.signIn(request)
                Log.d(TAG, "Código de respuesta: ${response.code()}")
                Log.d(TAG, "Respuesta recibida: ${response.body()}")
                Log.d(TAG, "Mensaje de error: ${response.errorBody()?.string()}")
                Log.d(TAG, "=== FIN REGISTRO ===")

                if (response.isSuccessful && response.body()?.status == "success") {
                    return@withContext Result.success(response.body()?.data!!)
                } else {
                    // Intentar obtener el mensaje de error del errorBody
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val errorResponse = gson.fromJson(errorBody, ApiResponse::class.java)
                            errorResponse.message ?: "Error en el registro"
                        } catch (e: Exception) {
                            "Error en el registro"
                        }
                    } else {
                        response.body()?.message ?: "Error en el registro"
                    }
                    Log.e(TAG, "Error en registro: $errorMessage")
                    return@withContext Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                when (e) {
                    is java.net.ProtocolException -> {
                        Log.e(TAG, "Error de conexión: ${e.message}")
                        return@withContext Result.failure(Exception("Error de conexión con el servidor. Por favor, inténtalo de nuevo."))
                    }
                    is java.net.SocketTimeoutException -> {
                        Log.e(TAG, "Timeout de conexión: ${e.message}")
                        return@withContext Result.failure(Exception("La conexión ha expirado. Por favor, inténtalo de nuevo."))
                    }
                    is java.net.UnknownHostException -> {
                        Log.e(TAG, "Error de host desconocido: ${e.message}")
                        return@withContext Result.failure(Exception("No se puede conectar con el servidor. Por favor, verifica tu conexión a internet."))
                    }
                    else -> {
                        Log.e(TAG, "Error inesperado: ${e.message}")
                        return@withContext Result.failure(Exception("Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo."))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en registro: ${e.message}", e)
            return@withContext Result.failure(Exception("Error al procesar la solicitud. Por favor, inténtalo de nuevo."))
        }
    }

    suspend fun getPerfil(idUsuario: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPerfil(idUsuario)
            if (response.isSuccessful && response.body()?.status == "success") {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Error al obtener perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePerfil(usuario: Usuario): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== INICIO ACTUALIZACIÓN PERFIL ===")
            Log.d(TAG, "Datos a actualizar: $usuario")
            
            val response = api.updatePerfil(usuario)
            Log.d(TAG, "Código de respuesta: ${response.code()}")
            Log.d(TAG, "Respuesta recibida: ${response.body()}")
            Log.d(TAG, "Error body: ${response.errorBody()?.string()}")
            
            // Si la respuesta es exitosa (200) pero el body es null, asumimos que la actualización fue exitosa
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.status == "success" || apiResponse == null) {
                    Log.d(TAG, "Perfil actualizado exitosamente")
                    // Si no recibimos datos actualizados, devolvemos el usuario que enviamos
                    return@withContext Result.success(apiResponse?.data ?: usuario)
                } else {
                    Log.e(TAG, "Error en actualización: ${apiResponse?.message}")
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error al actualizar perfil"))
                }
            }
            
            // Si no es exitosa, manejamos los diferentes códigos de error
            when (response.code()) {
                404 -> {
                    Log.e(TAG, "Usuario no encontrado")
                    return@withContext Result.failure(Exception("Usuario no encontrado"))
                }
                409 -> {
                    val apiResponse = response.body()
                    val errorMessage = when {
                        apiResponse?.message?.contains("email") == true -> "El email ya esta en uso por otro usuario"
                        apiResponse?.message?.contains("username") == true -> "El nombre de usuario ya esta en uso"
                        else -> apiResponse?.message ?: "Error de conflicto al actualizar perfil"
                    }
                    Log.e(TAG, "Error de conflicto: $errorMessage")
                    return@withContext Result.failure(Exception(errorMessage))
                }
                400 -> {
                    Log.e(TAG, "Campos vacíos")
                    return@withContext Result.failure(Exception("El nombre, email y nombre de usuario no pueden estar vacíos"))
                }
                500 -> {
                    val apiResponse = response.body()
                    val errorMessage = apiResponse?.message ?: "Error del servidor al actualizar perfil"
                    Log.e(TAG, "Error del servidor: $errorMessage")
                    return@withContext Result.failure(Exception(errorMessage))
                }
                else -> {
                    // Si la actualización fue exitosa en la base de datos pero hay un error en la respuesta
                    if (response.code() == 200) {
                        Log.d(TAG, "Actualización exitosa en la base de datos")
                        return@withContext Result.success(usuario)
                    }
                    val errorMessage = response.body()?.message ?: "Error desconocido al actualizar perfil"
                    Log.e(TAG, "Error desconocido: $errorMessage")
                    return@withContext Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en actualización: ${e.message}", e)
            return@withContext Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getAllUsuarios(): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllUsuarios()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener usuarios"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEstadoUsuario(idUsuario: String, estado: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== INICIO ACTUALIZACIÓN ESTADO ===")
            Log.d(TAG, "ID Usuario: $idUsuario, Nuevo estado: $estado")
            
            val request = mapOf(
                "id_usuario" to idUsuario,
                "estado" to estado
            )
            
            val response = api.updateEstadoUsuario(request)
            Log.d(TAG, "Código de respuesta: ${response.code()}")
            Log.d(TAG, "Respuesta recibida: ${response.body()}")
            
            if (response.isSuccessful && response.body()?.status == "success") {
                Log.d(TAG, "Estado actualizado exitosamente")
                return@withContext Result.success(true)
            }
            
            val errorMessage = response.body()?.message ?: "Error al actualizar estado"
            Log.e(TAG, "Error en actualización de estado: $errorMessage")
            return@withContext Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en actualización de estado: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun buscarUsuarios(query: String): List<Usuario> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando usuarios con query: $query")
            val response = api.buscarUsuarios(query)
            
            if (response.isSuccessful) {
                val usuarios = response.body() ?: emptyList()
                Log.d(TAG, "Usuarios encontrados: ${usuarios.size}")
                return@withContext usuarios
            }
            
            Log.e(TAG, "Error al buscar usuarios: ${response.errorBody()?.string()}")
            throw Exception("Error al buscar usuarios: ${response.errorBody()?.string()}")
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al buscar usuarios: ${e.message}", e)
            throw e
        }
    }

    companion object {
        private var instance: UsuarioRepository? = null

        fun getInstance(): UsuarioRepository {
            if (instance == null) {
                instance = UsuarioRepository()
            }
            return instance!!
        }
    }
} 