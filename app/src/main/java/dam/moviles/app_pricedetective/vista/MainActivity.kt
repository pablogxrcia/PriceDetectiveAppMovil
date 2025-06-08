package dam.moviles.app_pricedetective.vista

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val PREFS_NAME = "AppSettings"
    private val THEME_KEY_NEW = "theme_mode_new"

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar el tema antes de crear la vista
        applyTheme()
        super.onCreate(savedInstanceState)
        inicializarBinding()
        setContentView(binding.root)

        // Inicializar NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController

        // Mostrar tutorial si es la primera vez
        if (!getSharedPreferences("tutorial", 0).getBoolean("tutorial_shown", false)) {
            showTutorial()
        }
    }

    fun showTutorial() {
        // Mostrar el tutorial
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, TutorialFragment())
            .commit()
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean(THEME_KEY_NEW, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun inicializarBinding(){
        binding = ActivityMainBinding.inflate(layoutInflater)
    }
}