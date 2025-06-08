package dam.moviles.app_pricedetective.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.model.Usuario
import dam.moviles.app_pricedetective.databinding.ItemUsuarioBinding
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import android.graphics.Bitmap
import java.lang.ref.WeakReference
import dam.moviles.app_pricedetective.data.repository.AmigoRepository
import dam.moviles.app_pricedetective.data.SessionManager

class AnadirAmigoAdapter(
    private val listaUsuarios: List<Usuario>,
    private val onEnviarSolicitud: (Usuario) -> Unit
) : RecyclerView.Adapter<AnadirAmigoAdapter.UsuarioViewHolder>() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val jobMap = ConcurrentHashMap<Int, Job>()
    private val bitmapCache = ConcurrentHashMap<String, WeakReference<Bitmap>>()
    private var isDestroyed = false
    private val amigoRepo = AmigoRepository.getInstance()
    private val solicitudesPendientes = mutableMapOf<String, Boolean>()

    inner class UsuarioViewHolder(private val binding: ItemUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(usuario: Usuario, position: Int) {
            if (isDestroyed) return

            // Cancelar cualquier trabajo anterior para esta posición
            jobMap[position]?.cancel()

            // Mostrar información básica
            binding.tvUsername.text = usuario.username
            binding.tvEmail.text = usuario.email
            binding.ivAvatarUsuario.setImageResource(R.drawable.ic_person)

            // Verificar si hay una solicitud pendiente
            val job = coroutineScope.launch {
                try {
                    val userId = SessionManager(binding.root.context).getUser()?.idUsuario ?: return@launch
                    val amistades = amigoRepo.getAmistades(userId)
                    val tieneSolicitudPendiente = amistades.any { amistad ->
                        (amistad.usuario1 == userId && amistad.usuario2 == usuario.idUsuario ||
                        amistad.usuario1 == usuario.idUsuario && amistad.usuario2 == userId) &&
                        amistad.estado == "pendiente"
                    }
                    
                    solicitudesPendientes[usuario.idUsuario] = tieneSolicitudPendiente
                    
                    // Actualizar UI
                    binding.btnEnviarSolicitud.apply {
                        if (tieneSolicitudPendiente) {
                            setImageResource(R.drawable.baseline_hourglass_empty_24)
                            isEnabled = false
                            alpha = 0.5f
                        } else {
                            setImageResource(R.drawable.baseline_person_add_24)
                            isEnabled = true
                            alpha = 1.0f
                        }
                    }
                } catch (e: Exception) {
                    // En caso de error, mantener el botón habilitado
                    binding.btnEnviarSolicitud.apply {
                        setImageResource(R.drawable.baseline_person_add_24)
                        isEnabled = true
                        alpha = 1.0f
                    }
                }
            }

            // Configurar el botón de enviar solicitud
            binding.btnEnviarSolicitud.setOnClickListener {
                if (!solicitudesPendientes[usuario.idUsuario]!!) {
                    onEnviarSolicitud(usuario)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(listaUsuarios[position], position)
    }

    override fun getItemCount(): Int = listaUsuarios.size

    fun onDestroy() {
        isDestroyed = true
        coroutineScope.cancel()
        jobMap.clear()
        bitmapCache.clear()
        System.gc()
    }
} 