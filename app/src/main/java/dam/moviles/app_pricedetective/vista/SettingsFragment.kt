package dam.moviles.app_pricedetective.vista

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.data.model.ConfiguracionPrivacidad
import dam.moviles.app_pricedetective.data.repository.ConfiguracionPrivacidadRepository
import dam.moviles.app_pricedetective.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {
    private val PREFS_NAME = "AppSettings"
    private val THEME_KEY = "theme_mode"
    private val THEME_KEY_NEW = "theme_mode_new"
    private val TAG = "SettingsFragment"

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = checkNotNull(_binding) { "Uso incorrecto del objeto Binding" }

    private lateinit var sessionManager: SessionManager
    private val configuracionRepository = ConfiguracionPrivacidadRepository()
    private var currentConfig: ConfiguracionPrivacidad? = null
    private var isUpdating = false

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
        setupToolbar()
        setupDarkModeSwitch()
        setupTutorialButton()
        setupPrivacySwitches()
        loadPrivacySettings()
    }

    private fun setupToolbar() {
        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        
        toolbar?.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_principalFragment)
        }
    }

    private fun setupDarkModeSwitch() {
        val darkModeSwitch = view?.findViewById<SwitchMaterial>(R.id.darkModeSwitch)
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        var isDarkMode = prefs.getBoolean(THEME_KEY_NEW, false)

        if (!prefs.contains(THEME_KEY_NEW)) {
            try {
                val oldThemeMode = prefs.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_NO)
                isDarkMode = oldThemeMode == AppCompatDelegate.MODE_NIGHT_YES
                prefs.edit().apply {
                    putBoolean(THEME_KEY_NEW, isDarkMode)
                    remove(THEME_KEY)
                    apply()
                }
            } catch (e: Exception) {
                isDarkMode = false
            }
        }

        darkModeSwitch?.isChecked = isDarkMode

        darkModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().apply {
                putBoolean(THEME_KEY_NEW, isChecked)
                apply()
            }

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupTutorialButton() {
        binding.btnVerTutorial.setOnClickListener {
            requireActivity().getSharedPreferences("tutorial", 0)
                .edit()
                .putBoolean("tutorial_shown", false)
                .apply()
            
            (requireActivity() as MainActivity).showTutorial()
            
            if (findNavController().currentDestination?.id != R.id.principalFragment) {
                findNavController().navigate(R.id.action_settingsFragment_to_principalFragment)
            }
        }
    }

    private fun setupPrivacySwitches() {
        binding.switchUltimoAcceso.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdating) {
                updatePrivacySettings()
            }
        }

        binding.switchEstado.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdating) {
                updatePrivacySettings()
            }
        }
    }

    private fun loadPrivacySettings() {
        val currentUser = sessionManager.getUser()
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    configuracionRepository.getConfiguracionPrivacidad(currentUser.idUsuario)
                }.onSuccess { config ->
                    currentConfig = config
                    isUpdating = true
                    binding.switchUltimoAcceso.isChecked = config.mostrarUltimaConexionBoolean
                    binding.switchEstado.isChecked = config.mostrarEstadoBoolean
                    isUpdating = false
                }.onFailure { error ->
                    Log.e(TAG, "Error al cargar configuración: ${error.message}")
                    Toast.makeText(requireContext(), "Error al cargar la configuración", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado: ${e.message}", e)
                Toast.makeText(requireContext(), "Error inesperado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePrivacySettings() {
        if (isUpdating) return

        val currentUser = sessionManager.getUser()
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        val newConfig = ConfiguracionPrivacidad(
            idUsuario = currentUser.idUsuario,
            mostrarUltimaConexion = if (binding.switchUltimoAcceso.isChecked) 1 else 0,
            mostrarEstado = if (binding.switchEstado.isChecked) 1 else 0
        )

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    configuracionRepository.updateConfiguracionPrivacidad(newConfig)
                }.onSuccess {
                    currentConfig = newConfig
                    Toast.makeText(requireContext(), "Configuración actualizada", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Log.e(TAG, "Error al actualizar configuración: ${error.message}")
                    Toast.makeText(requireContext(), "Error al actualizar la configuración", Toast.LENGTH_SHORT).show()
                    // Revertir los switches a su estado anterior
                    isUpdating = true
                    currentConfig?.let { config ->
                        binding.switchUltimoAcceso.isChecked = config.mostrarUltimaConexionBoolean
                        binding.switchEstado.isChecked = config.mostrarEstadoBoolean
                    }
                    isUpdating = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado: ${e.message}", e)
                Toast.makeText(requireContext(), "Error inesperado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun inicializarBinding(inflater: LayoutInflater, container: ViewGroup?) {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}