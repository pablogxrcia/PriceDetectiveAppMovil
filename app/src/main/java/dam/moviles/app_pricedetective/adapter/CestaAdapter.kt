package dam.moviles.app_pricedetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.model.Product

class CestaAdapter(
    private var productos: List<Product>,
    private val onEliminarClick: (Product) -> Unit,
    private val onCantidadChanged: (Product) -> Unit
) : RecyclerView.Adapter<CestaAdapter.CestaViewHolder>() {

    inner class CestaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImageCesta)
        private val nombreProducto: TextView = itemView.findViewById(R.id.nombreProductoCesta)
        private val supermercado: TextView = itemView.findViewById(R.id.supermercadoCesta)
        private val cantidad: TextView = itemView.findViewById(R.id.cantidadCesta)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)
        private val btnMas: ImageButton = itemView.findViewById(R.id.btnMas)
        private val btnMenos: ImageButton = itemView.findViewById(R.id.btnMenos)

        fun bind(producto: Product) {
            nombreProducto.text = producto.productName
            supermercado.text = producto.stores
            cantidad.text = producto.cantidad.toString()

            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(producto.productImageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .centerCrop()
                .into(productImage)

            // Configurar los botones
            btnEliminar.setOnClickListener {
                onEliminarClick(producto)
            }

            btnMas.setOnClickListener {
                producto.cantidad++
                cantidad.text = producto.cantidad.toString()
                onCantidadChanged(producto)
            }

            btnMenos.setOnClickListener {
                if (producto.cantidad > 1) {
                    producto.cantidad--
                    cantidad.text = producto.cantidad.toString()
                    onCantidadChanged(producto)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CestaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cesta, parent, false)
        return CestaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CestaViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size

    fun actualizarLista(nuevaLista: List<Product>) {
        productos = nuevaLista
        notifyDataSetChanged()
    }
}
