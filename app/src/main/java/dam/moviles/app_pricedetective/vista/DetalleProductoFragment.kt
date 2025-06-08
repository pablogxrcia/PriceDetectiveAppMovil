package dam.moviles.app_pricedetective.vista

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.model.Product
import dam.moviles.app_pricedetective.databinding.FragmentDetalleProductoBinding
import dam.moviles.app_pricedetective.viewmodel.CestaViewModel

class DetalleProductoFragment : Fragment() {

    private var _binding: FragmentDetalleProductoBinding? = null
    private val binding get() = _binding!!
    private val args: DetalleProductoFragmentArgs by navArgs()
    private val cestaViewModel: CestaViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleProductoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        cargarDatosProducto()
        setupFloatingActionButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.navigationIcon?.setTint(resources.getColor(R.color.primary, null))
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun cargarDatosProducto() {
        // Configurar el título en el CollapsingToolbarLayout
        binding.collapsingToolbar.title = args.productName

        // Cargar la imagen del producto
        Glide.with(this)
            .load(args.productImageUrl)
            .centerCrop()
            .into(binding.productImage)

        // Configurar el resto de datos
        binding.txtProductName.text = args.productName
        binding.txtSupermarket.text = args.supermarket
        binding.txtDescription.text = "${args.unitPrice}€"
        
        // Generar y mostrar el código de barras si el EAN está disponible
        args.ean?.let { ean ->
            if (ean.isNotEmpty()) {
                generarCodigoBarras(ean)
            }
        }
    }

    private fun generarCodigoBarras(ean: String) {
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                ean,
                BarcodeFormat.EAN_13,
                800,
                200
            )
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            binding.barcodeImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupFloatingActionButton() {
        binding.btnAAdirCesta.setOnClickListener {
            val producto = Product(
                productName = args.productName,
                productImageUrl = args.productImageUrl,
                stores = args.supermarket,
                genericName = args.description,
                allergens = args.allergens,
                price = args.unitPrice.toDoubleOrNull() ?: 0.0,
                ean = args.ean
            )
            cestaViewModel.añadirProducto(producto)
            Toast.makeText(context, "Producto añadido a la cesta", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
