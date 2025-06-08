package dam.moviles.app_pricedetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.data.model.Product

class ProductAdapter(
    private val products: MutableList<Product>, // Lista visible de productos
    private val onItemClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit,
    private val onEndOfListReached: () -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val allProducts = mutableListOf<Product>() // Lista completa de productos (sin filtrar)
    private var isLoadingMore = false
    private var lastPosition = -1
    private var shouldTriggerLoadMore = true

    // Método para acceder a la lista completa
    fun getAllProducts(): List<Product> = allProducts

    // Método para actualizar la lista filtrada
    fun updateProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val addToCartButton: View = itemView.findViewById(R.id.btnAddCesta)

        fun bind(product: Product) {
            productName.text = product.productName
            productPrice.text = String.format("%.2f €", product.price ?: 0.0)

            Glide.with(itemView.context)
                .load(product.productImageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .centerCrop()
                .into(productImage)

            itemView.setOnClickListener { onItemClick(product) }

            addToCartButton.setOnClickListener { onAddToCartClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
        
        if (position == products.size - 1 && shouldTriggerLoadMore && !isLoadingMore) {
            shouldTriggerLoadMore = false
            isLoadingMore = true
            onEndOfListReached()
        }
    }

    override fun getItemCount(): Int = products.size

    // Método para agregar nuevos productos a la lista
    fun addProducts(newProducts: List<Product>) {
        val oldSize = products.size
        products.addAll(newProducts)
        allProducts.addAll(newProducts) // Guardamos los productos en la lista completa
        notifyItemRangeInserted(oldSize, newProducts.size)
        isLoadingMore = false
        shouldTriggerLoadMore = true
    }

    // Método para limpiar la lista visible
    fun clearProducts() {
        products.clear()
        allProducts.clear()
        isLoadingMore = false
        shouldTriggerLoadMore = true
        notifyDataSetChanged()
    }
}
