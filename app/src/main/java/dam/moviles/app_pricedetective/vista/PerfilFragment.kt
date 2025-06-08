package dam.moviles.app_pricedetective.vista

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.data.model.Usuario
import dam.moviles.app_pricedetective.data.repository.UsuarioRepository
import dam.moviles.app_pricedetective.databinding.FragmentPerfilBinding
import kotlinx.coroutines.launch
import java.io.IOException

class PerfilFragment : Fragment() {
    private var _binding: FragmentPerfilBinding? = null
    private val binding: FragmentPerfilBinding
        get() = checkNotNull(_binding) { "uso incorrecto del objeto Binding" }

    private var selectedImageUri: Uri? = null
    private lateinit var sessionManager: SessionManager
    private val repository = UsuarioRepository()
    private var currentUser: Usuario? = null

    private fun convertImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Comprimir la imagen
            val maxSize = 800
            val width = originalBitmap.width
            val height = originalBitmap.height
            
            val scale = if (width > height) {
                maxSize.toFloat() / width
            } else {
                maxSize.toFloat() / height
            }
            
            val scaledWidth = (width * scale).toInt()
            val scaledHeight = (height * scale).toInt()
            
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                originalBitmap,
                scaledWidth,
                scaledHeight,
                true
            )
            
            // Convertir a JPEG con calidad razonable
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream) // Calidad del 80%
            val bytes = outputStream.toByteArray()
            
            // Liberar memoria
            originalBitmap.recycle()
            scaledBitmap.recycle()
            
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            Log.d("PerfilFragment", "Tamaño de la imagen en Base64: ${base64.length} caracteres")
            
            // Verificar si el tamaño es razonable (menos de 5MB en Base64)
            if (base64.length > 5000000) {
                Log.e("PerfilFragment", "La imagen es demasiado grande (más de 5MB)")
                return null
            }
            
            base64
        } catch (e: Exception) {
            Log.e("PerfilFragment", "Error al convertir imagen a Base64: ${e.message}")
            null
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                    binding.profileImage.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    Log.e("PerfilFragment", "Error al cargar la imagen: ${e.message}")
                    Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            inicializarBinding(inflater, container)
            sessionManager = SessionManager(requireContext())
            return binding.root
        } catch (e: Exception) {
            Log.e("PerfilFragment", "Error en onCreateView: ${e.message}")
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            loadUserData()
            setupListeners()
            setupTextInputListeners()
        } catch (e: Exception) {
            Log.e("PerfilFragment", "Error en onViewCreated: ${e.message}")
            Toast.makeText(requireContext(), "Error al inicializar la vista", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        currentUser = sessionManager.getUser()
        currentUser?.let { usuario ->
            binding.txtNombre.setText(usuario.nombre)
            binding.txtEmail.setText(usuario.email)
            binding.txtNombreUsuario.setText(usuario.username)
            // Cargar imagen de perfil si existe
            usuario.fotoPerfil?.let { fotoPerfil ->
                if (fotoPerfil.isNotEmpty()) {
                    try {
                        val decodedBytes = android.util.Base64.decode(fotoPerfil, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        binding.profileImage.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("PerfilFragment", "Error al cargar la imagen de perfil: ${e.message}")
                    }
                }
            }
        }
    }

    private fun setupTextInputListeners() {
        try {
            val textInputs = arrayOf(
                binding.txtNombre.parent as? TextInputLayout,
                binding.txtEmail.parent as? TextInputLayout,
                binding.txtNombreUsuario.parent as? TextInputLayout
            ).filterNotNull()

            textInputs.forEach { textInputLayout ->
                textInputLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
                    val colorRes = if (hasFocus) R.color.orange else R.color.white
                    val color = ContextCompat.getColor(requireContext(), colorRes)
                    textInputLayout.setHintTextColor(android.content.res.ColorStateList.valueOf(color))
                }
            }
        } catch (e: Exception) {
            Log.e("PerfilFragment", "Error en setupTextInputListeners: ${e.message}")
        }
    }

    private fun setupListeners() {
        try {
            // Configurar el botón de retroceso en la toolbar
            binding.toolbar.setNavigationOnClickListener {
                findNavController().navigate(R.id.action_perfilFragment_to_principalFragment)
            }

            // Configurar el botón de cambiar foto
            binding.btnChangePhoto.setOnClickListener {
                openImagePicker()
            }

            // Configurar el botón de guardar
            binding.btnGuardar.setOnClickListener {
                saveChanges()
            }
        } catch (e: Exception) {
            Log.e("PerfilFragment", "Error en setupListeners: ${e.message}")
        }
    }

    private fun saveChanges() {
        val nombre = binding.txtNombre.text.toString()
        val email = binding.txtEmail.text.toString()
        val username = binding.txtNombreUsuario.text.toString()

        Log.d("PerfilFragment", "=== INICIO GUARDAR CAMBIOS ===")
        Log.d("PerfilFragment", "Datos a guardar - Nombre: $nombre, Email: $email, Username: $username")

        if (nombre.isEmpty() || email.isEmpty() || username.isEmpty()) {
            Log.e("PerfilFragment", "Campos vacíos detectados")
            Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.let { usuario ->
            Log.d("PerfilFragment", "Usuario actual: $usuario")
            
            // Convertir la imagen seleccionada a Base64 si existe
            val fotoPerfilBase64 = selectedImageUri?.let { uri ->
                Log.d("PerfilFragment", "Procesando nueva imagen de perfil")
                convertImageToBase64(uri)
            }

            if (fotoPerfilBase64 == null && selectedImageUri != null) {
                Log.e("PerfilFragment", "Error al procesar la imagen - demasiado grande")
                Toast.makeText(requireContext(), "La imagen es demasiado grande. Por favor, selecciona una imagen más pequeña.", Toast.LENGTH_SHORT).show()
                return
            }

            val updatedUser = usuario.copy(
                nombre = nombre,
                email = email,
                username = username,
                fotoPerfil = fotoPerfilBase64 ?: usuario.fotoPerfil
            )

            Log.d("PerfilFragment", "Usuario actualizado: $updatedUser")
            Log.d("PerfilFragment", "Enviando actualización de perfil con foto: ${fotoPerfilBase64 != null}")

            lifecycleScope.launch {
                try {
                    Log.d("PerfilFragment", "Iniciando actualización en el repositorio")
                    repository.updatePerfil(updatedUser)
                        .onSuccess { updatedUsuario ->
                            Log.d("PerfilFragment", "Actualización exitosa: $updatedUsuario")
                            sessionManager.saveUser(updatedUsuario)
                            Toast.makeText(requireContext(), "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_perfilFragment_to_principalFragment)
                        }
                        .onFailure { error ->
                            Log.e("PerfilFragment", "Error al actualizar perfil: ${error.message}")
                            Log.e("PerfilFragment", "Stack trace: ${error.stackTraceToString()}")
                            Toast.makeText(requireContext(), "Error al actualizar el perfil: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Log.e("PerfilFragment", "Error de conexión: ${e.message}")
                    Log.e("PerfilFragment", "Stack trace: ${e.stackTraceToString()}")
                    Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Log.e("PerfilFragment", "No hay usuario actual en la sesión")
            Toast.makeText(requireContext(), "Error: No hay usuario en sesión", Toast.LENGTH_SHORT).show()
        }
        Log.d("PerfilFragment", "=== FIN GUARDAR CAMBIOS ===")
    }

    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            pickImage.launch(intent)
        } catch (e: Exception) {
            Log.e("PerfilFragment", "Error al abrir el selector de imágenes: ${e.message}")
            Toast.makeText(requireContext(), "Error al abrir el selector de imágenes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inicializarBinding(inflater: LayoutInflater, container: ViewGroup?) {
        try {
            _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        } catch (e: Exception) {
            Log.e("PerfilFragment", "Error al inicializar el binding: ${e.message}")
            throw e
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
