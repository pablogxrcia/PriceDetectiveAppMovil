package dam.moviles.app_pricedetective.data.model

data class Product(
    val productName: String,
    val genericName: String?,
    val stores: String?,
    val allergens: String?,
    val productImageUrl: String?,
    val price: Double? = null,
    val category: String? = null,
    val subcategory: String? = null,
    val ean: String? = null,
    var cantidad: Int = 1 // nuevo campo
)
