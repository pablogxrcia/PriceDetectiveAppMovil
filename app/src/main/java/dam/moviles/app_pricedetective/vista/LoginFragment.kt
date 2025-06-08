package dam.moviles.app_pricedetective.vista

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.data.repository.UsuarioRepository
import dam.moviles.app_pricedetective.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding
        get() = checkNotNull(_binding) { "uso incorrecto del objeto Binding" }

    private val repository = UsuarioRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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
            findNavController().navigate(R.id.action_loginFragment_to_principalFragment)
            return
        }

        binding.registerTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    repository.login(email, password)
                        .onSuccess { usuario ->
                            // Guardar la sesión
                            sessionManager.saveUser(usuario)
                            // Actualizar estado a activo
                            repository.updateEstadoUsuario(usuario.idUsuario, "activo")
                                .onSuccess {
                                    Log.d("LoginFragment", "Estado actualizado a activo")
                                }
                                .onFailure { error ->
                                    Log.e("LoginFragment", "Error al actualizar estado: ${error.message}")
                                }
                            // Login exitoso
                            Toast.makeText(context, "Login exitoso", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_principalFragment)
                        }
                        .onFailure { error ->
                            // Mostrar mensaje de error
                            Toast.makeText(context, error.message ?: "Error en el login", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun inicializarBinding(inflater: LayoutInflater, container: ViewGroup?) {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}