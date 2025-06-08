package dam.moviles.app_pricedetective.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dam.moviles.app_pricedetective.data.model.Product

class CestaViewModel : ViewModel() {

    private val _productosEnCesta = MutableLiveData<List<Product>>(mutableListOf())
    val productosEnCesta: LiveData<List<Product>> get() = _productosEnCesta

    fun añadirProducto(producto: Product) {
        // Obtenemos la lista actual como mutable para poder modificarla
        val listaActual = _productosEnCesta.value?.toMutableList() ?: mutableListOf()

        // Buscamos si el producto ya está en la cesta (mismo nombre y tienda)
        val productoExistente = listaActual.find {
            it.productName == producto.productName && it.stores == producto.stores
        }

        if (productoExistente != null) {
            // Incrementamos la cantidad del producto existente
            productoExistente.cantidad += 1
        } else {
            // Si es nuevo, añadimos con cantidad 1 (asegúrate de que producto.cantidad sea 1 al crear)
            producto.cantidad = 1
            listaActual.add(producto)
        }

        // Actualizamos el LiveData con la nueva lista
        _productosEnCesta.value = listaActual
    }

    fun actualizarCantidadProducto(producto: Product) {
        val listaActual = _productosEnCesta.value?.toMutableList() ?: mutableListOf()
        val productoExistente = listaActual.find {
            it.productName == producto.productName && it.stores == producto.stores
        }

        if (productoExistente != null) {
            productoExistente.cantidad = producto.cantidad
            _productosEnCesta.value = listaActual
        }
    }

    fun eliminarProducto(producto: Product) {
        val listaActual = _productosEnCesta.value?.toMutableList() ?: mutableListOf()
        val productoExistente = listaActual.find {
            it.productName == producto.productName && it.stores == producto.stores
        }

        if (productoExistente != null) {
            if (productoExistente.cantidad > 1) {
                productoExistente.cantidad -= 1
            } else {
                listaActual.remove(productoExistente)
            }
            _productosEnCesta.value = listaActual
        }
    }

    fun vaciarCesta() {
        _productosEnCesta.value = emptyList()
    }
}
