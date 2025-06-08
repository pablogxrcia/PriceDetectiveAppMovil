package dam.moviles.app_pricedetective.vista

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import dam.moviles.app_pricedetective.adapter.AmigosAdapter
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.data.repository.AmigoRepository
import dam.moviles.app_pricedetective.databinding.FragmentTodosAmigosBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TodosAmigosFragment : Fragment() {
    private var _binding: FragmentTodosAmigosBinding? = null
    private val binding get() = _binding!!
    private val repo = AmigoRepository.getInstance()
    private var adapter: AmigosAdapter? = null
    private var isFragmentActive = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodosAmigosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        cargarAmigos()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewTodosAmigos.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun cargarAmigos() {
        val userId = SessionManager(requireContext()).getUser()?.idUsuario ?: return
        Log.d("TodosAmigosFragment", "Cargando amigos para usuario: $userId")
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val lista = repo.getAmigos(userId)
                Log.d("TodosAmigosFragment", "Lista de amigos obtenida: ${lista.size}")
                
                if (!isFragmentActive || !isAdded) return@launch
                
                if (lista.isEmpty()) {
                    Log.d("TodosAmigosFragment", "No se encontraron amigos")
                    binding.recyclerViewTodosAmigos.visibility = View.GONE
                    binding.tvNoAmigos.visibility = View.VISIBLE
                } else {
                    Log.d("TodosAmigosFragment", "Mostrando lista de amigos")
                    binding.tvNoAmigos.visibility = View.GONE
                    binding.recyclerViewTodosAmigos.visibility = View.VISIBLE
                    
                    // Limpiar el adaptador anterior si existe
                    adapter?.onDestroy()
                    
                    // Crear nuevo adaptador
                    adapter = AmigosAdapter(lista, userId)
                    binding.recyclerViewTodosAmigos.adapter = adapter
                }
            } catch (e: Exception) {
                if (!isFragmentActive || !isAdded) return@launch
                Log.e("TodosAmigosFragment", "Error al cargar amigos", e)
                Toast.makeText(requireContext(),
                    "Error al cargar amigos: ${e.message}",
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isFragmentActive = false
        adapter?.onDestroy()
        adapter = null
        _binding = null
    }
}