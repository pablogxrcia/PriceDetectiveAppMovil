package dam.moviles.app_pricedetective.data.model

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("code") val code: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("generic_name") val genericName: String?,
    @SerializedName("stores") val stores: String?,
    @SerializedName("allergens") val allergens: String?,
    @SerializedName("image_url") val imageUrl: String?
) {
    fun toDomainProduct(): Product? {
        // Solo creamos el producto si tiene al menos un nombre
        if (productName.isNullOrEmpty()) return null

        return Product(
            productName = productName,
            genericName = genericName,
            stores = stores,
            allergens = allergens,
            productImageUrl = imageUrl
        )
    }
}
