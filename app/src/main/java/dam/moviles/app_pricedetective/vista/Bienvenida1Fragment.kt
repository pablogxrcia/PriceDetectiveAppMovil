package dam.moviles.app_pricedetective.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.databinding.FragmentBienvenida1Binding

class Bienvenida1Fragment : Fragment() {
    private var _binding: FragmentBienvenida1Binding? = null
    private val binding: FragmentBienvenida1Binding
        get() = checkNotNull(_binding) { "uso incorrecto del objeto Binding" }

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inicializarBinding(inflater, container)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar si hay una sesión activa
        if (sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.action_bienvenida1Fragment_to_principalFragment)
            return
        }

        binding.btnSiguiente.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_bienvenida1Fragment_to_bienvenida2Fragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun inicializarBinding(inflater: LayoutInflater, container: ViewGroup?) {
        _binding = FragmentBienvenida1Binding.inflate(inflater, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}