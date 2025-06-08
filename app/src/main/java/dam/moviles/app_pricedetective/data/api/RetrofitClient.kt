package dam.moviles.app_pricedetective.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //private const val BASE_URL = "http://localhost/apiPriceDetective/"
    const val BASE_URL = "http://13.219.200.170//apiPriceDetective/" // 10.0.2.2 es localhost para el emulador

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val usuarioApi: UsuarioApi by lazy {
        retrofit.create(UsuarioApi::class.java)
    }

    val amigoApi: AmigoApi by lazy {
        retrofit.create(AmigoApi::class.java)
    }

    val configuracionApi: ConfiguracionApi by lazy {
        retrofit.create(ConfiguracionApi::class.java)
    }
} 