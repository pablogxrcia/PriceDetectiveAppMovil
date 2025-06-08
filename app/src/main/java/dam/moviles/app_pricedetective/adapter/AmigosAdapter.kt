package dam.moviles.app_pricedetective.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.model.Amistad
import dam.moviles.app_pricedetective.data.model.Usuario
import dam.moviles.app_pricedetective.data.repository.UsuarioRepository
import dam.moviles.app_pricedetective.data.repository.ConfiguracionPrivacidadRepository
import dam.moviles.app_pricedetective.databinding.ItemAmigoBinding
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import android.util.Log
import android.graphics.Bitmap
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale

class AmigosAdapter(
    private val listaAmigos: List<Amistad>,
    private val usuarioActualId: String
) : RecyclerView.Adapter<AmigosAdapter.AmigoViewHolder>() {

    private val usuarioRepo = UsuarioRepository()
    private val configuracionRepo = ConfiguracionPrivacidadRepository()
    private val cacheUsuarios = ConcurrentHashMap<String, Usuario>()
    private val cacheConfiguraciones = ConcurrentHashMap<String, Boolean>()
    private val jobMap = ConcurrentHashMap<Int, Job>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isDestroyed = false
    private val bitmapCache = ConcurrentHashMap<String, WeakReference<Bitmap>>()

    inner class AmigoViewHolder(private val binding: ItemAmigoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(amigo: Amistad, position: Int) {
            if (isDestroyed) return

            // Cancelar cualquier trabajo anterior para esta posición
            jobMap[position]?.cancel()
            
            // Determinamos el "otro" usuario que no es el actual
            val idAmigo = if (amigo.usuario1 == usuarioActualId) amigo.usuario2 else amigo.usuario1

            // Primero mostrar el ID mientras se carga
            binding.tvNombreAmigo.text = idAmigo
            binding.ivAvatarAmigo.setImageResource(R.drawable.ic_person)

            // Iniciar la carga del usuario en segundo plano
            val job = coroutineScope.launch {
                try {
                    // Intentar obtener del caché primero
                    var usuario = cacheUsuarios[idAmigo]
                    if (usuario == null) {
                        // Si no está en caché, cargar de la API
                        val resultado = usuarioRepo.getPerfil(idAmigo)
                        if (resultado.isSuccess) {
                            usuario = resultado.getOrNull()
                            usuario?.let { cacheUsuarios[idAmigo] = it }
                        }
                    }

                    // Obtener la configuración de privacidad del usuario
                    var mostrarUltimoAcceso = cacheConfiguraciones[idAmigo]
                    if (mostrarUltimoAcceso == null) {
                        val configResult = configuracionRepo.getConfiguracionPrivacidad(idAmigo)
                        if (configResult.isSuccess) {
                            mostrarUltimoAcceso = configResult.getOrNull()?.mostrarUltimaConexionBoolean ?: true
                            cacheConfiguraciones[idAmigo] = mostrarUltimoAcceso
                        } else {
                            mostrarUltimoAcceso = true // Por defecto mostrar
                        }
                    }

                    // Actualizar la UI con los datos del usuario
                    usuario?.let { user ->
                        binding.tvNombreAmigo.text = user.username
                        
                        // Mostrar último acceso solo si está permitido
                        if (mostrarUltimoAcceso == true) {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val fecha = inputFormat.parse(user.ultimoAcceso)
                            binding.tvUltimoAcceso.text = "Último acceso: ${fecha?.let { outputFormat.format(it) } ?: "N/A"}"
                            binding.tvUltimoAcceso.visibility = ViewGroup.VISIBLE
                        } else {
                            binding.tvUltimoAcceso.visibility = ViewGroup.GONE
                        }

                        // Actualizar el estado del círculo según el estado del usuario y su configuración
                        val mostrarEstado = configuracionRepo.getConfiguracionPrivacidad(idAmigo).getOrNull()?.mostrarEstadoBoolean ?: true
                        if (mostrarEstado) {
                            binding.ivEstadoAmigo.setImageResource(
                                if (user.estado == "activo") R.drawable.ic_circle_green
                                else R.drawable.ic_circle_red
                            )
                            binding.ivEstadoAmigo.visibility = ViewGroup.VISIBLE
                        } else {
                            binding.ivEstadoAmigo.setImageResource(R.drawable.ic_circle_gray)
                            binding.ivEstadoAmigo.visibility = ViewGroup.VISIBLE
                        }

                        // Cargar la imagen del perfil
                        user.fotoPerfil?.let { fotoBase64 ->
                            try {
                                val bitmap = bitmapCache[fotoBase64]?.get()
                                if (bitmap != null) {
                                    binding.ivAvatarAmigo.setImageBitmap(bitmap)
                                } else {
                                    val decodedBytes = Base64.decode(fotoBase64, Base64.DEFAULT)
                                    val newBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    bitmapCache[fotoBase64] = WeakReference(newBitmap)
                                    binding.ivAvatarAmigo.setImageBitmap(newBitmap)
                                }
                            } catch (e: Exception) {
                                Log.e("AmigosAdapter", "Error al cargar imagen", e)
                                binding.ivAvatarAmigo.setImageResource(R.drawable.ic_person)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AmigosAdapter", "Error al cargar datos del usuario", e)
                }
            }
            jobMap[position] = job
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmigoViewHolder {
        val binding = ItemAmigoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AmigoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AmigoViewHolder, position: Int) {
        holder.bind(listaAmigos[position], position)
    }

    override fun getItemCount(): Int = listaAmigos.size

    fun onDestroy() {
        isDestroyed = true
        coroutineScope.cancel()
        jobMap.clear()
        cacheUsuarios.clear()
        cacheConfiguraciones.clear()
        bitmapCache.clear()
        System.gc()
    }
}
