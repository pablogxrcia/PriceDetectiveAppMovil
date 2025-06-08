package dam.moviles.app_pricedetective.vista

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.databinding.FragmentCestaBinding
import dam.moviles.app_pricedetective.viewmodel.CestaViewModel
import dam.moviles.app_pricedetective.adapter.CestaAdapter
import dam.moviles.app_pricedetective.data.model.Product
import dam.moviles.app_pricedetective.util.PdfGenerator

class CestaFragment : Fragment() {

    private var _binding: FragmentCestaBinding? = null
    private val binding: FragmentCestaBinding
        get() = checkNotNull(_binding) { "Uso incorrecto del objeto Binding" }

    private val cestaViewModel: CestaViewModel by activityViewModels()
    private lateinit var cestaAdapter: CestaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inicializarBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeCesta()
    }

    private fun setupRecyclerView() {
        cestaAdapter = CestaAdapter(
            emptyList(),
            onEliminarClick = { producto ->
                mostrarDialogoConfirmacion(producto)
            },
            onCantidadChanged = { producto ->
                cestaViewModel.actualizarCantidadProducto(producto)
            }
        )

        binding.recyclerViewCesta.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cestaAdapter
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_cestaFragment_to_principalFragment)
        }

        binding.imgVaciarCesta.setOnClickListener {
            mostrarDialogoVaciarCesta()
        }

        binding.btnDescargarLista.setOnClickListener {
            descargarListaCompra()
        }
    }

    private fun observeCesta() {
        cestaViewModel.productosEnCesta.observe(viewLifecycleOwner) { productos ->
            cestaAdapter.actualizarLista(productos)
            actualizarVisibilidadCestaVacia(productos.isEmpty())
            actualizarTotalProductos(productos)
        }
    }

    private fun actualizarTotalProductos(productos: List<Product>) {
        val totalProductos = productos.sumOf { it.cantidad }
        binding.tvTotalItems.text = if (totalProductos == 1) "1 item" else "$totalProductos items"
        binding.tvStatus.text = if (productos.isEmpty()) "Tu cesta está vacía" else "Productos en tu cesta"
        binding.tvTotalProductos.text = totalProductos.toString()
        
        // Calcular el total estimado
        val totalEstimado = productos.sumOf { (it.price ?: 0.0) * it.cantidad }
        binding.tvTotalPrecio.text = String.format("%.2f €", totalEstimado)
    }

    private fun mostrarDialogoConfirmacion(producto: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Estás seguro de que quieres eliminar este producto de la cesta?")
            .setPositiveButton("Sí") { dialog, _ ->
                cestaViewModel.eliminarProducto(producto)
                Toast.makeText(requireContext(), "Producto eliminado de la cesta", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarDialogoVaciarCesta() {
        AlertDialog.Builder(requireContext())
            .setTitle("Vaciar Cesta")
            .setMessage("¿Estás seguro de que quieres vaciar la cesta?")
            .setPositiveButton("Sí") { dialog, _ ->
                cestaViewModel.vaciarCesta()
                Toast.makeText(requireContext(), "Cesta vaciada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun descargarListaCompra() {
        val productos = cestaViewModel.productosEnCesta.value ?: return
        
        if (productos.isEmpty()) {
            Toast.makeText(requireContext(), "La cesta está vacía", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pdfGenerator = PdfGenerator(requireContext())
            val file = pdfGenerator.generarListaCompra(productos)
            
            // Compartir el PDF
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(intent, "Compartir lista de la compra"))
            
            Toast.makeText(requireContext(), "Lista de la compra generada", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al generar la lista: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun actualizarVisibilidadCestaVacia(estaVacia: Boolean) {
        binding.imgCestaVacia.visibility = if (estaVacia) View.VISIBLE else View.GONE
        binding.recyclerViewCesta.visibility = if (estaVacia) View.GONE else View.VISIBLE
    }

    private fun inicializarBinding(inflater: LayoutInflater, container: ViewGroup?) {
        _binding = FragmentCestaBinding.inflate(inflater, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
