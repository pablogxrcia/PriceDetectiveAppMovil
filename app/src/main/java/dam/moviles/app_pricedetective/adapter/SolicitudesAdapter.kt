package dam.moviles.app_pricedetective.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.model.Amistad
import dam.moviles.app_pricedetective.data.model.Usuario
import dam.moviles.app_pricedetective.data.repository.UsuarioRepository
import dam.moviles.app_pricedetective.databinding.ItemSolicitudAmistadBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class SolicitudesAdapter(
    private val listaSolicitudes: List<Amistad>,
    private val onAceptar: (Amistad) -> Unit,
    private val onRechazar: (Amistad) -> Unit
) : RecyclerView.Adapter<SolicitudesAdapter.SolicitudViewHolder>() {

    private val usuarioRepo = UsuarioRepository()
    private val cacheUsuarios = ConcurrentHashMap<String, Usuario>()
    private val jobMap = ConcurrentHashMap<Int, Job>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    inner class SolicitudViewHolder(private val binding: ItemSolicitudAmistadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(solicitud: Amistad, position: Int) {
            // Cancelar cualquier trabajo anterior para esta posición
            jobMap[position]?.cancel()
            
            // Obtener el ID del usuario que envía la solicitud
            val idUsuarioSolicitante = solicitud.usuario1

            // Primero mostrar el ID mientras se carga
            binding.tvNombreUsuario.text = idUsuarioSolicitante
            binding.ivAvatarUsuario.setImageResource(R.drawable.ic_person)

            // Cargar información del usuario
            val job = coroutineScope.launch {
                try {
                    // Verificar si el usuario está en caché
                    val usuario = cacheUsuarios[idUsuarioSolicitante] ?: withContext(Dispatchers.IO) {
                        val result = usuarioRepo.getPerfil(idUsuarioSolicitante)
                        result.getOrNull()?.also { cacheUsuarios[idUsuarioSolicitante] = it }
                    }

                    usuario?.let {
                        binding.tvNombreUsuario.text = it.username

                        // Cargar foto de perfil si existe
                        if (!it.fotoPerfil.isNullOrEmpty()) {
                            try {
                                withContext(Dispatchers.IO) {
                                    // Decodificar base64 a bitmap
                                    val decodedString = Base64.decode(it.fotoPerfil, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                }?.let { bitmap ->
                                    binding.ivAvatarUsuario.setImageBitmap(bitmap)
                                }
                            } catch (e: Exception) {
                                binding.ivAvatarUsuario.setImageResource(R.drawable.ic_person)
                            }
                        } else {
                            binding.ivAvatarUsuario.setImageResource(R.drawable.ic_person)
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    // Si hay error, mantener el ID y el icono por defecto
                }
            }
            
            // Guardar la referencia al job
            jobMap[position] = job

            // Formatear la fecha
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fecha = inputFormat.parse(solicitud.fechaSolicitud)
            binding.tvFechaSolicitud.text = fecha?.let { outputFormat.format(it) }

            binding.btnAceptar.setOnClickListener {
                onAceptar(solicitud)
            }

            binding.btnRechazar.setOnClickListener {
                onRechazar(solicitud)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val binding = ItemSolicitudAmistadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SolicitudViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        holder.bind(listaSolicitudes[position], position)
    }

    override fun getItemCount(): Int = listaSolicitudes.size

    fun onDestroy() {
        coroutineScope.cancel()
        jobMap.clear()
        cacheUsuarios.clear()
    }
} 