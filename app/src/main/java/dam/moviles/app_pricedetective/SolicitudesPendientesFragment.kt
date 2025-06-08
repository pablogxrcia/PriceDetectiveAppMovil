package dam.moviles.app_pricedetective

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import dam.moviles.app_pricedetective.adapter.SolicitudesAdapter
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.data.model.Amistad
import dam.moviles.app_pricedetective.data.repository.AmigoRepository
import dam.moviles.app_pricedetective.databinding.FragmentSolicitudesPendientesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class SolicitudesPendientesFragment : Fragment() {

    private var _binding: FragmentSolicitudesPendientesBinding? = null
    private val binding get() = _binding!!
    private val repo = AmigoRepository.getInstance()
    private var adapter: SolicitudesAdapter? = null
    private var isFragmentActive = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSolicitudesPendientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarSolicitudes()
    }

    private fun cargarSolicitudes() {
        val userId = SessionManager(requireContext()).getUser()?.idUsuario ?: return
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val lista = repo.getSolicitudesRecibidas(userId)
                if (!isFragmentActive || !isAdded) return@launch
                
                if (lista.isEmpty()) {
                    binding.recyclerViewSolicitudes.visibility = View.GONE
                    binding.tvNoSolicitudes.visibility = View.VISIBLE
                } else {
                    binding.tvNoSolicitudes.visibility = View.GONE
                    binding.recyclerViewSolicitudes.visibility = View.VISIBLE
                    
                    // Limpiar el adaptador anterior si existe
                    adapter?.onDestroy()
                    
                    // Crear nuevo adaptador
                    adapter = SolicitudesAdapter(
                        lista,
                        onAceptar = { solicitud -> aceptarSolicitud(solicitud) },
                        onRechazar = { solicitud -> rechazarSolicitud(solicitud) }
                    )
                    binding.recyclerViewSolicitudes.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerViewSolicitudes.adapter = adapter
                }
            } catch (e: Exception) {
                if (!isFragmentActive || !isAdded) return@launch
                Log.e("SolicitudesFragment", "Error al cargar solicitudes", e)
                Toast.makeText(requireContext(),
                    "Error al cargar solicitudes: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun aceptarSolicitud(solicitud: Amistad) {
        Log.d("SolicitudesFragment", "Intentando aceptar solicitud: ${solicitud.id}")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Primero intentamos responder la solicitud
                repo.responderSolicitud(solicitud.id, "aceptada")
                Log.d("SolicitudesFragment", "Solicitud aceptada exitosamente")
                
                // Esperamos un momento para asegurarnos de que la base de datos se actualice
                kotlinx.coroutines.delay(500)
                
                // Recargamos la lista
                cargarSolicitudes()
                
                Toast.makeText(requireContext(),
                    "Solicitud aceptada",
                    Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SolicitudesFragment", "Error al aceptar solicitud", e)
                Toast.makeText(requireContext(),
                    "Error al aceptar solicitud: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun rechazarSolicitud(solicitud: Amistad) {
        Log.d("SolicitudesFragment", "Intentando rechazar solicitud: ${solicitud.id}")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                repo.eliminarAmistad(solicitud.usuario1, solicitud.usuario2)
                Log.d("SolicitudesFragment", "Solicitud rechazada exitosamente")
                
                // Esperamos un momento para asegurarnos de que la base de datos se actualice
                kotlinx.coroutines.delay(500)
                
                // Recargamos la lista
                cargarSolicitudes()
                
                Toast.makeText(requireContext(),
                    "Solicitud rechazada",
                    Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SolicitudesFragment", "Error al rechazar solicitud", e)
                Toast.makeText(requireContext(),
                    "Error al rechazar solicitud: ${e.message}",
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