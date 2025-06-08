package dam.moviles.app_pricedetective

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dam.moviles.app_pricedetective.adapter.AnadirAmigoAdapter
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.data.model.Usuario
import dam.moviles.app_pricedetective.data.repository.AmigoRepository
import dam.moviles.app_pricedetective.data.repository.UsuarioRepository
import dam.moviles.app_pricedetective.databinding.FragmentAnadirAmigoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.util.Log

class AnadirAmigoFragment : Fragment() {

    private var _binding: FragmentAnadirAmigoBinding? = null
    private val binding get() = _binding!!
    private val amigoRepo = AmigoRepository.getInstance()
    private val usuarioRepo = UsuarioRepository()
    private var adapter: AnadirAmigoAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnadirAmigoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        cargarUsuarios()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewResultados.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun cargarUsuarios() {
        val userId = SessionManager(requireContext()).getUser()?.idUsuario ?: return
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Obtener todos los usuarios
                val resultadoUsuarios = usuarioRepo.getAllUsuarios()
                // Obtener amigos actuales
                val amigosActuales = amigoRepo.getAmigos(userId)
                
                resultadoUsuarios.onSuccess { usuarios ->
                    // Filtrar usuarios que no son el usuario actual y no son amigos
                    val usuariosFiltrados = usuarios.filter { usuario ->
                        usuario.idUsuario != userId && 
                        !amigosActuales.any { amigo -> 
                            amigo.usuario1 == usuario.idUsuario || amigo.usuario2 == usuario.idUsuario 
                        }
                    }
                    
                    if (usuariosFiltrados.isEmpty()) {
                        binding.recyclerViewResultados.visibility = View.GONE
                        binding.tvNoResultados.visibility = View.VISIBLE
                    } else {
                        binding.tvNoResultados.visibility = View.GONE
                        binding.recyclerViewResultados.visibility = View.VISIBLE
                        
                        // Limpiar el adaptador anterior si existe
                        adapter?.onDestroy()
                        
                        // Crear nuevo adaptador
                        adapter = AnadirAmigoAdapter(usuariosFiltrados) { usuario ->
                            enviarSolicitudAmistad(usuario)
                        }
                        binding.recyclerViewResultados.adapter = adapter
                    }
                }.onFailure { error ->
                    Log.e("AnadirAmigoFragment", "Error al cargar usuarios", error)
                    Toast.makeText(requireContext(),
                        "Error al cargar usuarios: ${error.message}",
                        Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("AnadirAmigoFragment", "Error al cargar usuarios", e)
                Toast.makeText(requireContext(),
                    "Error al cargar usuarios: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun enviarSolicitudAmistad(usuario: Usuario) {
        val userId = SessionManager(requireContext()).getUser()?.idUsuario ?: return
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                amigoRepo.enviarSolicitud(userId, usuario.idUsuario)
                Toast.makeText(requireContext(),
                    "Solicitud enviada a ${usuario.username}",
                    Toast.LENGTH_SHORT).show()
                
                // Esperar un momento para asegurar que la base de datos se actualice
                delay(500)
                
                // Recargar la lista de usuarios
                cargarUsuarios()
            } catch (e: Exception) {
                Log.e("AnadirAmigoFragment", "Error al enviar solicitud", e)
                Toast.makeText(requireContext(),
                    "Error al enviar solicitud: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter?.onDestroy()
        _binding = null
    }
}