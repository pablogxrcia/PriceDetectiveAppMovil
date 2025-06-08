package dam.moviles.app_pricedetective.vista

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.adapter.CarouselAdapter
import dam.moviles.app_pricedetective.adapter.ProductAdapter
import dam.moviles.app_pricedetective.data.SessionManager
import dam.moviles.app_pricedetective.data.model.Product
import dam.moviles.app_pricedetective.data.repository.ProductRepository
import dam.moviles.app_pricedetective.data.repository.UsuarioRepository
import dam.moviles.app_pricedetective.databinding.FragmentPrincipalBinding
import dam.moviles.app_pricedetective.viewmodel.CestaViewModel
import kotlinx.coroutines.launch
import com.google.android.material.navigation.NavigationView
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class PrincipalFragment : Fragment() {

    private var _binding: FragmentPrincipalBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productRepository: ProductRepository
    private val cestaViewModel: CestaViewModel by activityViewModels()
    private val repository = UsuarioRepository()
    private lateinit var carouselAdapter: CarouselAdapter
    private var carouselCallback: ViewPager2.OnPageChangeCallback? = null

    private var currentPage = 1
    private var isLoading = false
    private var currentQuery = ""
    private var hasMoreProducts = true
    private val productsPerPage = 10
    private var currentSortOrder: SortOrder = SortOrder.NONE
    private var isFirstLoad = true
    private var savedProducts = mutableListOf<Product>()
    private var currentCategory: String? = null
    private var currentSubcategory: String? = null
    private var lastVisibleItemPosition = 0

    enum class SortOrder {
        NONE, LOW_TO_HIGH, HIGH_TO_LOW
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPrincipalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        productRepository = ProductRepository(requireContext())
        
        setupToolbar()
        setupNavigationDrawer()
        setupCardClickListeners()
        setupCarousel()
        setupRecyclerView()
        setupSearchView()
        setupFabCesta()

        // Asegurar que el buscador esté desactivado inicialmente
        binding.searchView.isEnabled = false
        binding.searchView.editText?.isEnabled = false
        binding.searchView.editText?.isFocusable = false
        binding.searchView.editText?.isFocusableInTouchMode = false
        binding.searchView.editText?.text?.clear()
        binding.searchView.clearFocus()
        hideKeyboard()

        // Asegurar que el FAB esté visible
        binding.fabCesta.visibility = View.VISIBLE

        if (savedProducts.isEmpty()) {
            loadInitialProducts()
        } else {
            productAdapter.updateProducts(savedProducts)
        }
    }

    private fun setupCarousel() {
        carouselAdapter = CarouselAdapter(listOf(
            R.drawable.carrousel1,
            R.drawable.ca,
            R.drawable.carrousel3
        ))
        
        binding.viewPagerCarousel.adapter = carouselAdapter

        // Auto-scroll del carrusel
        carouselCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isAdded && _binding != null) {
                    binding.viewPagerCarousel.postDelayed({
                        if (isAdded && _binding != null) {
                            if (position == carouselAdapter.itemCount - 1) {
                                binding.viewPagerCarousel.setCurrentItem(0, true)
                            } else {
                                binding.viewPagerCarousel.setCurrentItem(position + 1, true)
                            }
                        }
                    }, 3000) // Cambiar imagen cada 3 segundos
                }
            }
        }
        binding.viewPagerCarousel.registerOnPageChangeCallback(carouselCallback!!)
    }

    private fun setupCardClickListeners() {
        binding.cardPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_principalFragment_to_perfilFragment)
        }

        binding.cardAmigos.setOnClickListener {
            findNavController().navigate(R.id.action_principalFragment_to_amigosFragment)
        }

        binding.cardCesta.setOnClickListener {
            findNavController().navigate(R.id.action_principalFragment_to_cestaFragment)
        }

        binding.cardAjustes.setOnClickListener {
            findNavController().navigate(R.id.action_principalFragment_to_settingsFragment)
        }
    }

    private fun setupNavigationDrawer() {
        binding.materialToolbar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.categoria_aceite_especias_salsas -> {
                    currentCategory = "Aceite, especias y salsas"
                    currentSubcategory = null
                    binding.searchView.editText?.setText("")
                    showProductsView()
                    searchProducts("")
                }
                R.id.subcategoria_aceite_vinagre_sal -> {
                    currentCategory = "Aceite, especias y salsas"
                    currentSubcategory = "Aceite, vinagre y sal"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_especias -> {
                    currentCategory = "Aceite, especias y salsas"
                    currentSubcategory = "Especias"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_mayonesa_ketchup_mostaza -> {
                    currentCategory = "Aceite, especias y salsas"
                    currentSubcategory = "Mayonesa, ketchup y mostaza"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_otras_salsas -> {
                    currentCategory = "Aceite, especias y salsas"
                    currentSubcategory = "Otras salsas"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.categoria_agua_refrescos -> {
                    currentCategory = "Agua y refrescos"
                    currentSubcategory = null
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_agua -> {
                    currentCategory = "Agua y refrescos"
                    currentSubcategory = "Agua"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_isotonicos_energeticos -> {
                    currentCategory = "Agua y refrescos"
                    currentSubcategory = "Isotónico y energético"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_refresco_cola -> {
                    currentCategory = "Agua y refrescos"
                    currentSubcategory = "Refresco de cola"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_refresco_naranja_limon -> {
                    currentCategory = "Agua y refrescos"
                    currentSubcategory = "Refresco de naranja y de limón"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_tonica_bitter -> {
                    currentCategory = "Agua y refrescos"
                    currentSubcategory = "Tónica y bitter"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_refresco_te_sin_gas -> {
                    currentCategory = "Agua y refrescos"
                    currentSubcategory = "Refresco de té y sin gas"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.categoria_aperitivos -> {
                    currentCategory = "Aperitivos"
                    currentSubcategory = null
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_aceitunas_encurtidos -> {
                    currentCategory = "Aperitivos"
                    currentSubcategory = "Aceitunas y encurtidos"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_frutos_secos -> {
                    currentCategory = "Aperitivos"
                    currentSubcategory = "Frutos secos y fruta desecada"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_patatas_snacks -> {
                    currentCategory = "Aperitivos"
                    currentSubcategory = "Patatas fritas y snacks"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.categoria_arroz_legumbres_pasta -> {
                    currentCategory = "Arroz, legumbres y pasta"
                    currentSubcategory = null
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_arroz -> {
                    currentCategory = "Arroz, legumbres y pasta"
                    currentSubcategory = "Arroz"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_legumbres -> {
                    currentCategory = "Arroz, legumbres y pasta"
                    currentSubcategory = "Legumbres"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_pasta_fideos -> {
                    currentCategory = "Arroz, legumbres y pasta"
                    currentSubcategory = "Pasta y fideos"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.categoria_azucar_caramelos_chocolate -> {
                    currentCategory = "Azúcar, caramelos y chocolate"
                    currentSubcategory = null
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_azucar_edulcorante -> {
                    currentCategory = "Azúcar, caramelos y chocolate"
                    currentSubcategory = "Azúcar y edulcorante"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_chicles_caramelos -> {
                    currentCategory = "Azúcar, caramelos y chocolate"
                    currentSubcategory = "Chicles y caramelos"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_chocolate -> {
                    currentCategory = "Azúcar, caramelos y chocolate"
                    currentSubcategory = "Chocolate"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
                R.id.subcategoria_golosinas -> {
                    currentCategory = "Azúcar, caramelos y chocolate"
                    currentSubcategory = "Golosinas"
                    binding.searchView.editText?.setText("")
                    searchProducts("")
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupToolbar() {
        binding.materialToolbar.title = ""
        val activity = requireActivity() as MainActivity
        activity.setSupportActionBar(binding.materialToolbar)
        activity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_principal, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.perfil -> {
                        findNavController().navigate(R.id.action_principalFragment_to_perfilFragment)
                        true
                    }
                    R.id.amigos -> {
                        findNavController().navigate(R.id.action_principalFragment_to_amigosFragment)
                        true
                    }
                    R.id.fabCesta -> {
                        findNavController().navigate(R.id.action_principalFragment_to_cestaFragment)
                        true
                    }
                    R.id.logOut -> {
                        val sessionManager = SessionManager(requireContext())
                        val currentUser = sessionManager.getUser()

                        // Actualizar estado a inactivo antes de cerrar sesión
                        lifecycleScope.launch {
                            try {
                                currentUser?.let { usuario ->
                                    repository.updateEstadoUsuario(usuario.idUsuario, "inactivo")
                                        .onSuccess {
                                            Log.d("PrincipalFragment", "Estado actualizado a inactivo")
                                        }
                                        .onFailure { error ->
                                            Log.e("PrincipalFragment", "Error al actualizar estado: ${error.message}")
                                        }
                                }
                                // Cerrar sesión
                                sessionManager.clearSession()
                                if (isAdded) {
                                    Toast.makeText(activity, getString(R.string.session_closed), Toast.LENGTH_SHORT).show()
                                }
                                findNavController().navigate(R.id.action_principalFragment_to_loginFragment)
                            } catch (e: Exception) {
                                Log.e("PrincipalFragment", "Error al cerrar sesión: ${e.message}")
                            }
                        }
                        true
                    }
                    R.id.settings -> {
                        findNavController().navigate(R.id.action_principalFragment_to_settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onItemClick = { product -> showProductDetails(product) },
            onAddToCartClick = { product -> addToCart(product) },
            onEndOfListReached = { loadMoreProducts() }
        )

        binding.recyclerViewProductos.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun applyFilters() {
        val allProducts = productAdapter.getAllProducts()
        val filteredProducts = when (currentSortOrder) {
            SortOrder.LOW_TO_HIGH -> allProducts.sortedBy { it.price ?: Double.MAX_VALUE }
            SortOrder.HIGH_TO_LOW -> allProducts.sortedByDescending { it.price ?: 0.0 }
            SortOrder.NONE -> allProducts
        }
        productAdapter.updateProducts(filteredProducts)
    }

    private fun setupSearchView() {
        binding.searchView.editText?.addTextChangedListener(object : android.text.TextWatcher {
            private var searchJob: Job? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300) // Esperar 300ms antes de realizar la búsqueda
                    val query = s?.toString() ?: ""
                    searchProducts(query)
                }
            }
        })

        binding.searchView.setEndIconOnClickListener {
            binding.searchView.editText?.text?.clear()
            hideKeyboard()
        }
    }

    private fun loadInitialProducts() {
        if (isLoading) return
        
        isLoading = true
        showLoading(true)
        currentPage = 1
        hasMoreProducts = true
        savedProducts.clear()
        productAdapter.clearProducts()
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val products = productRepository.searchProducts(
                    query = currentQuery,
                    page = currentPage,
                    pageSize = productsPerPage,
                    category = currentCategory,
                    subcategory = currentSubcategory
                )
                
                if (products.isNotEmpty()) {
                    savedProducts.addAll(products)
                    productAdapter.updateProducts(products)
                    hasMoreProducts = products.size == productsPerPage
                } else {
                    hasMoreProducts = false
                    if (isAdded) {
                        Toast.makeText(requireContext(), "No se encontraron productos", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("PrincipalFragment", "Error al cargar productos iniciales: ${e.message}")
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
                showLoading(false)
            }
        }
    }

    private fun loadMoreProducts() {
        if (isLoading || !hasMoreProducts) return
        
        isLoading = true
        currentPage++
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val products = productRepository.getProductsByCategory(
                    category = currentCategory,
                    subcategory = currentSubcategory,
                    page = currentPage,
                    pageSize = productsPerPage
                )
                productAdapter.addProducts(products)
                hasMoreProducts = products.size == productsPerPage
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar más productos", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    private fun searchProducts(query: String) {
        Log.d("PrincipalFragment", "Buscando productos con query: $query")
        showProductsView()
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (query.isEmpty()) {
                    // Si la búsqueda está vacía, mostrar productos de la categoría actual
                    val products = productRepository.getProductsByCategory(
                        category = currentCategory,
                        subcategory = currentSubcategory,
                        page = 1,
                        pageSize = Int.MAX_VALUE
                    )
                    Log.d("PrincipalFragment", "Mostrando productos de categoría: ${products.size}")
                    productAdapter.updateProducts(products)
                } else {
                    // Si hay búsqueda, buscar en todos los productos
                    val products = productRepository.getProductsByCategory(
                        category = null,
                        subcategory = null,
                        page = 1,
                        pageSize = Int.MAX_VALUE
                    )
                    val filteredProducts = products.filter { product ->
                        product.productName.contains(query, ignoreCase = true)
                    }
                    Log.d("PrincipalFragment", "Productos encontrados en búsqueda global: ${filteredProducts.size}")
                    productAdapter.updateProducts(filteredProducts)
                }
            } catch (e: Exception) {
                Log.e("PrincipalFragment", "Error al buscar productos", e)
                Toast.makeText(context, "Error al buscar productos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProductDetails(product: Product) {
        val navController = findNavController()
        val currentDestination = navController.currentDestination?.id
        
        // Solo navegar si no estamos ya en el fragmento de detalle
        if (currentDestination != R.id.detalleProductoFragment) {
            val action = PrincipalFragmentDirections
                .actionPrincipalFragmentToDetalleProductoFragment(
                    productName = product.productName,
                    productImageUrl = product.productImageUrl ?: "",
                    supermarket = product.stores ?: "",
                    unitPrice = product.price?.toString() ?: "0.0",
                    description = product.genericName ?: "",
                    allergens = product.allergens ?: "",
                    ean = product.ean ?: ""
                )
            navController.navigate(action)
        }
    }

    private fun addToCart(product: Product) {
        cestaViewModel.añadirProducto(product)
        Toast.makeText(requireContext(), "Producto añadido a la cesta", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        // Eliminado el código del progressBar ya que no existe en el layout
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showProductsView() {
        binding.viewPagerCarousel.visibility = View.GONE
        binding.homeContent.visibility = View.GONE
        binding.recyclerViewProductos.visibility = View.VISIBLE
        binding.searchView.visibility = View.VISIBLE
        binding.fabCesta.visibility = View.VISIBLE
        binding.searchView.isEnabled = true
        binding.searchView.editText?.isEnabled = true
        binding.searchView.editText?.isFocusable = true
        binding.searchView.editText?.isFocusableInTouchMode = true
        Log.d("PrincipalFragment", "Mostrando vista de productos")
    }

    private fun showHomeView() {
        binding.viewPagerCarousel.visibility = View.VISIBLE
        binding.homeContent.visibility = View.VISIBLE
        binding.recyclerViewProductos.visibility = View.GONE
        binding.searchView.visibility = View.GONE
        binding.fabCesta.visibility = View.VISIBLE
        binding.searchView.isEnabled = false
        binding.searchView.editText?.isEnabled = false
        binding.searchView.editText?.isFocusable = false
        binding.searchView.editText?.isFocusableInTouchMode = false
        binding.searchView.editText?.text?.clear()
        binding.searchView.clearFocus()
        hideKeyboard()
        Log.d("PrincipalFragment", "Mostrando vista principal")
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun loadProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val products = productRepository.getProductsByCategory(
                    category = currentCategory,
                    subcategory = currentSubcategory,
                    page = 1,
                    pageSize = Int.MAX_VALUE
                )
                
                if (products.isNotEmpty()) {
                    productAdapter.updateProducts(products)
                    Log.d("PrincipalFragment", "Productos cargados: ${products.size}")
                } else {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "No se encontraron productos", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("PrincipalFragment", "Error al cargar productos: ${e.message}")
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFabCesta() {
        binding.fabCesta.setOnClickListener {
            findNavController().navigate(R.id.action_principalFragment_to_cestaFragment)
        }
        // Asegurar que el FAB esté visible
        binding.fabCesta.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        carouselCallback?.let {
            binding.viewPagerCarousel.unregisterOnPageChangeCallback(it)
        }
        carouselCallback = null
        _binding = null
    }
}
