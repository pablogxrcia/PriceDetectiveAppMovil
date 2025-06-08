package dam.moviles.app_pricedetective.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.data.repository.UsuarioRepository
import dam.moviles.app_pricedetective.databinding.FragmentRegistroBinding
import kotlinx.coroutines.launch

class RegistroFragment : Fragment() {
    private var _binding: FragmentRegistroBinding?=null
    private val binding: FragmentRegistroBinding
        get()= checkNotNull(_binding) {"uso incorrecto del objeeto Binding"}

    private val repository = UsuarioRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inicializarBinding(inflater,container)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_registroFragment_to_loginFragment)
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.confirmPasswordEditText.text.toString()
            val username = binding.usernameEditText.text.toString()
            val nombre = binding.nameEditText.text.toString()

            // Limpiar errores previos
            clearErrors()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    repository.signIn(nombre, email, username, password)
                        .onSuccess { usuario ->
                            // Guardar la sesión
                            sessionManager.saveUser(usuario)
                            // Registro exitoso
                            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registroFragment_to_principalFragment)
                        }
                        .onFailure { error ->
                            // Manejar errores específicos
                            when {
                                error.message?.contains("Correo electrónico inválido") == true -> {
                                    showError(binding.emailLayout, "Correo electrónico inválido")
                                }
                                error.message?.contains("El nombre de usuario ya está en uso") == true -> {
                                    showError(binding.usernameLayout, "El nombre de usuario ya está en uso")
                                }
                                error.message?.contains("El correo electrónico ya está en uso") == true -> {
                                    showError(binding.emailLayout, "El correo electrónico ya está en uso")
                                }
                                else -> {
                                    Toast.makeText(context, error.message ?: "Error en el registro", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showError(textInputLayout: TextInputLayout, errorMessage: String) {
        textInputLayout.error = errorMessage
    }

    private fun clearErrors() {
        binding.emailLayout.error = null
        binding.usernameLayout.error = null
        binding.nameLayout.error = null
        binding.passwordLayout.error = null
    }

    private fun inicializarBinding(inflater: LayoutInflater,container: ViewGroup?){
        _binding= FragmentRegistroBinding.inflate(inflater,container,false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}
