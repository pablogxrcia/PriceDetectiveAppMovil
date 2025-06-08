package dam.moviles.app_pricedetective

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dam.moviles.app_pricedetective.adapter.AmigosAdapter
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.data.repository.AmigoRepository
import dam.moviles.app_pricedetective.data.repository.UsuarioRepository
import dam.moviles.app_pricedetective.databinding.FragmentAmigosOnlineBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class AmigosOnlineFragment : Fragment() {
    //
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentAmigosOnlineBinding? = null
    private val binding get() = _binding!!
    private val amigoRepo = AmigoRepository.getInstance()
    private val usuarioRepo = UsuarioRepository()
    private var adapter: AmigosAdapter? = null
    private var isFragmentActive = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAmigosOnlineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        cargarAmigosOnline()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewAmigosOnline.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun cargarAmigosOnline() {
        val userId = SessionManager(requireContext()).getUser()?.idUsuario ?: return
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Obtener todos los amigos
                val amigos = amigoRepo.getAmigos(userId)
                
                // Filtrar solo los amigos activos
                val amigosActivos = amigos.filter { amigo ->
                    val idAmigo = if (amigo.usuario1 == userId) amigo.usuario2 else amigo.usuario1
                    val usuario = usuarioRepo.getPerfil(idAmigo).getOrNull()
                    usuario?.estado == "activo"
                }

                if (!isFragmentActive || !isAdded) return@launch

                if (amigosActivos.isEmpty()) {
                    binding.recyclerViewAmigosOnline.visibility = View.GONE
                    binding.tvNoAmigosOnline.visibility = View.VISIBLE
                } else {
                    binding.tvNoAmigosOnline.visibility = View.GONE
                    binding.recyclerViewAmigosOnline.visibility = View.VISIBLE
                    
                    // Limpiar el adaptador anterior si existe
                    adapter?.onDestroy()
                    
                    // Crear nuevo adaptador
                    adapter = AmigosAdapter(amigosActivos, userId)
                    binding.recyclerViewAmigosOnline.adapter = adapter
                }
            } catch (e: Exception) {
                if (!isFragmentActive || !isAdded) return@launch
                Log.e("AmigosOnlineFragment", "Error al cargar amigos online", e)
                Toast.makeText(requireContext(),
                    "Error al cargar amigos online: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isFragmentActive = false
    }

    override fun onResume() {
        super.onResume()
        isFragmentActive = true
        cargarAmigosOnline()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isFragmentActive = false
        adapter?.onDestroy()
        adapter = null
        _binding = null
    }
}