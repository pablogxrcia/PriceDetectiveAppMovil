package dam.moviles.app_pricedetective.data.model

data class MercadonaProduct(
    val id: String,
    val nombre: String,
    val thumbnail: String,
    val unit_price: String,
    val ean: String,
    val category: String,
    val subcategory: String
) {
    fun toDomainProduct(): Product {
        return Product(
            productName = nombre,
            productImageUrl = thumbnail,
            price = unit_price.toDoubleOrNull() ?: 0.0,
            ean = ean,
            category = category,
            subcategory = subcategory,
            stores = "Mercadona",
            genericName = null,
            allergens = null
        )
    }
}

data class MercadonaResponse(
    val totalProductos: Int,
    val productos: List<MercadonaProduct>
) 