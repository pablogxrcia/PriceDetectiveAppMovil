package dam.moviles.app_pricedetective.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import dam.moviles.app_pricedetective.AmigosOnlineFragment
import dam.moviles.app_pricedetective.AnadirAmigoFragment
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.SolicitudesPendientesFragment
import dam.moviles.app_pricedetective.databinding.FragmentAmigosBinding

class AmigosFragment : Fragment() {
    private var _binding: FragmentAmigosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAmigosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupBottomNavigation()
        showFragment(TodosAmigosFragment()) // Fragmento inicial
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.amigos_online -> {
                    showFragment(AmigosOnlineFragment())
                    true
                }
                R.id.todos_amigos -> {
                    showFragment(TodosAmigosFragment())
                    true
                }
                R.id.solicitudes_pendientes -> {
                    showFragment(SolicitudesPendientesFragment())
                    true
                }
                R.id.anadir_amigo -> {
                    showFragment(AnadirAmigoFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        childFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
