package dam.moviles.app_pricedetective.data.api

import retrofit2.http.GET
import retrofit2.http.QueryMap

interface OpenFoodFactsApi {
    @GET("cgi/search.pl")
    suspend fun searchProducts(@QueryMap params: Map<String, String>): OpenFoodFactsResponse
}
