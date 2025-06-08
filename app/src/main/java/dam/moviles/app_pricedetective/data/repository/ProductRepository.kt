package dam.moviles.app_pricedetective.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonElement
import dam.moviles.app_pricedetective.data.model.MercadonaProduct
import dam.moviles.app_pricedetective.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(private val context: Context) {
    private var allProducts: List<MercadonaProduct>? = null
    private val TAG = "ProductRepository"
    private val cache = mutableMapOf<String, List<Product>>()
    private val cacheTimeout = 5 * 60 * 1000 // 5 minutos en milisegundos
    private var lastCacheUpdate = 0L
    private val gson = Gson()

    private fun loadAllProducts(): List<MercadonaProduct> {
        // Si los productos ya están en caché y no han expirado, los devolvemos
        if (allProducts != null && System.currentTimeMillis() - lastCacheUpdate < cacheTimeout) {
            return allProducts!!
        }

        try {
            val jsonString = context.assets.open("mercadona_products.json").bufferedReader().use { it.readText() }
            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
            val products = mutableListOf<MercadonaProduct>()

            // Obtener el objeto "categorias"
            val categoriasObj = jsonObject.getAsJsonObject("categorias")
            
            // Iterar sobre las categorías
            categoriasObj.entrySet().forEach { (categoriaNombre, categoriaValue) ->
                val categoriaObj = categoriaValue.asJsonObject
                
                // Iterar sobre las subcategorías
                categoriaObj.entrySet().forEach { (subcategoriaNombre, subcategoriaValue) ->
                    val productosArray = subcategoriaValue.asJsonArray
                    
                    // Iterar sobre los productos
                    productosArray.forEach { productoElement ->
                        val producto = productoElement.asJsonObject
                        val mercadonaProduct = MercadonaProduct(
                            id = producto.get("id").asString,
                            nombre = producto.get("nombre").asString,
                            thumbnail = producto.get("thumbnail").asString,
                            unit_price = producto.get("unit_price").asString,
                            ean = producto.get("ean").asString,
                            category = categoriaNombre,
                            subcategory = subcategoriaNombre
                        )
                        products.add(mercadonaProduct)
                    }
                }
            }
            
            allProducts = products
            lastCacheUpdate = System.currentTimeMillis()
            Log.d(TAG, "Productos cargados: ${products.size}")
            return products
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar productos: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun searchProducts(
        query: String,
        page: Int,
        pageSize: Int = 20,
        category: String? = null,
        subcategory: String? = null
    ): List<Product> = withContext(Dispatchers.IO) {
        val cacheKey = "${query}_${category}_${subcategory}"
        
        // Verificar si tenemos los resultados en caché
        if (cache.containsKey(cacheKey)) {
            val cachedResults = cache[cacheKey]!!
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, cachedResults.size)
            return@withContext cachedResults.subList(startIndex, endIndex)
        }

        try {
            val products = loadAllProducts()
            Log.d(TAG, "Total de productos cargados: ${products.size}")
            
            // Filtrar productos según la búsqueda y categorías
            val filteredProducts = products.filter { product ->
                val matchesQuery = query.isEmpty() || product.nombre.contains(query, ignoreCase = true)
                val matchesCategory = category == null || product.category == category
                val matchesSubcategory = subcategory == null || product.subcategory == subcategory
                
                matchesQuery && matchesCategory && matchesSubcategory
            }
            
            Log.d(TAG, "Productos filtrados: ${filteredProducts.size}")
            
            // Convertir a Product y guardar en caché
            val domainProducts = filteredProducts.map { it.toDomainProduct() }
            cache[cacheKey] = domainProducts
            
            // Paginar los resultados
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, domainProducts.size)
            
            val result = domainProducts.subList(startIndex, endIndex)
            Log.d(TAG, "Productos a devolver: ${result.size}")
            return@withContext result
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar productos: ${e.message}")
            return@withContext emptyList()
        }
    }

    fun clearCache() {
        allProducts = null
        cache.clear()
        lastCacheUpdate = 0L
    }

    suspend fun getProductsByCategory(
        category: String?,
        subcategory: String?,
        page: Int,
        pageSize: Int = 20
    ): List<Product> = withContext(Dispatchers.IO) {
        val cacheKey = "category_${category}_${subcategory}"
        
        // Verificar si tenemos los resultados en caché
        if (cache.containsKey(cacheKey)) {
            val cachedResults = cache[cacheKey]!!
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, cachedResults.size)
            return@withContext cachedResults.subList(startIndex, endIndex)
        }

        try {
            val products = loadAllProducts()
            
            // Filtrar productos según las categorías
            val filteredProducts = products.filter { product ->
                val matchesCategory = category == null || product.category == category
                val matchesSubcategory = subcategory == null || product.subcategory == subcategory
                
                matchesCategory && matchesSubcategory
            }
            
            // Convertir a Product y guardar en caché
            val domainProducts = filteredProducts.map { it.toDomainProduct() }
            cache[cacheKey] = domainProducts
            
            // Paginar los resultados
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, domainProducts.size)
            
            return@withContext domainProducts.subList(startIndex, endIndex)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener productos por categoría: ${e.message}")
            return@withContext emptyList()
        }
    }
}
