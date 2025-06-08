package dam.moviles.app_pricedetective.data.model

fun ProductDto.toDomainProduct(): Product? = code?.let {
    Product(
        productName = productName ?: "Producto sin nombre",
        genericName = genericName,
        stores = stores,
        allergens = allergens,
        productImageUrl = imageUrl
    )
}


