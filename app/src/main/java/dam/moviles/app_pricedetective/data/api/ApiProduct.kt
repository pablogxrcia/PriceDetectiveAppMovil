package dam.moviles.app_pricedetective.data.api

import dam.moviles.app_pricedetective.data.model.Product

data class ApiProduct(
    val code: String,
    val product_name: String?,
    val generic_name: String?,
    val stores: String?,
    val allergens: String?,
    val image_url: String?  // Cambiado de image_nutrition_url a image_url
) {
    fun toDomainProduct(): Product? {
        if (product_name.isNullOrEmpty()) return null

        return Product(
            productName = product_name,
            genericName = generic_name,
            stores = stores,
            allergens = allergens,
            productImageUrl = image_url  // Cambiado de nutritionImageUrl a productImageUrl
        )
    }
}
