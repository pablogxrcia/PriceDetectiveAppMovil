package dam.moviles.app_pricedetective.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.databinding.FragmentBienvenida1Binding
import dam.moviles.app_pricedetective.databinding.FragmentBienvenida2Binding

class Bienvenida2Fragment : Fragment() {
    private var _binding:FragmentBienvenida2Binding?=null
    private val binding:FragmentBienvenida2Binding
        get()= checkNotNull(_binding) {"uso incorrecto del objeeto Binding"}
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inicializarBinding(inflater,container)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSiguiente.setOnClickListener {
            // Navegar al siguiente fragment usando NavController
            findNavController().navigate(R.id.action_bienvenida2Fragment_to_loginFragment)
        }
    }

    private fun inicializarBinding(inflater: LayoutInflater,container: ViewGroup?){
        _binding=FragmentBienvenida2Binding.inflate(inflater,container,false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}