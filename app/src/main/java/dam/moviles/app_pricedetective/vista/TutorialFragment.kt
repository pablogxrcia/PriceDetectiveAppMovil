package dam.moviles.app_pricedetective.vista

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import dam.moviles.app_pricedetective.R
import dam.moviles.app_pricedetective.databinding.FragmentTutorialBinding

class TutorialFragment : Fragment() {

    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!

    private lateinit var tutorialSteps: List<TutorialStep>
    private var currentStep = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTutorialSteps()
        setupNavigation()
        showCurrentStep()
    }

    private fun setupTutorialSteps() {
        tutorialSteps = listOf(
            TutorialStep(
                "¡Bienvenido a PriceDetective!",
                "Tu asistente personal para encontrar las mejores ofertas. Vamos a explorar las principales funciones.",
                null
            ),
            TutorialStep(
                "Búsqueda de Productos",
                "Usa la barra de búsqueda para encontrar cualquier producto. PriceDetective buscará en múltiples tiendas.",
                R.id.searchView
            ),
            TutorialStep(
                "Visualización de Productos",
                "Pincha en cualquier producto y podrás ver su informacion mas detallada.",
                R.id.recyclerViewProductos
            ),
            TutorialStep(
                "Contenido de la cesta",
                "Podrás ver los productos que has añadido a la cesta pulsando el icono de cesta en la barra de tareas o en el boton de abajo a la derecha.",
                R.id.fabCesta
            ),
            TutorialStep(
                "Gestión de Amigos",
                "En el apartado de amigos podrás ver tus amigos, añadir nuevos y gestionar tus solicitudes de amistad.",
                R.id.amigos
            ),
            TutorialStep(
                "Personalización de tu perfil",
                "Pulsando el icono de perfil de la barra de tareas podras personalizar tu perfil.",
                R.id.perfil
            ),
            TutorialStep(
                "Configuración a tu gusto",
                "En el apartado de configuración podrás configurar la aplicacion como desees.",
                R.id.settings
            ),
        )

        // Configurar el indicador de progreso
        binding.progressIndicator.max = 100
        updateProgress()
    }

    private fun updateProgress() {
        val progress = ((currentStep.toFloat() / (tutorialSteps.size - 1)) * 100).toInt()
        binding.progressIndicator.progress = progress
    }

    private fun setupNavigation() {
        binding.skipButton.setOnClickListener {
            dismissTutorial()
        }

        binding.nextButton.setOnClickListener {
            if (currentStep < tutorialSteps.size - 1) {
                currentStep++
                showCurrentStep()
            } else {
                dismissTutorial()
            }
        }

        binding.previousButton.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                showCurrentStep()
            }
        }
    }

    private fun showCurrentStep() {
        val step = tutorialSteps[currentStep]

        // Actualizar UI
        binding.tutorialTitle.text = step.title
        binding.tutorialDescription.text = step.description

        // Actualizar botones
        binding.previousButton.visibility = if (currentStep > 0) View.VISIBLE else View.GONE
        binding.nextButton.text = if (currentStep == tutorialSteps.size - 1) "Finalizar" else "Siguiente"

        // Actualizar indicador de progreso
        updateProgress()
    }

    private fun dismissTutorial() {
        // Guardar que el tutorial ya se mostró
        requireActivity().getSharedPreferences("tutorial", 0)
            .edit()
            .putBoolean("tutorial_shown", true)
            .apply()

        // Mostrar animación de finalización
        showCompletionAnimation()
    }

    private fun showCompletionAnimation() {
        // Inflar el layout de finalización
        val completionView = layoutInflater.inflate(R.layout.tutorial_completion_dialog, null)
        
        // Configurar el layout params para centrar en la pantalla
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        }

        // Añadir la vista al contenedor
        binding.root.addView(completionView, params)

        // Configurar la animación inicial
        completionView.alpha = 0f
        completionView.scaleX = 0.5f
        completionView.scaleY = 0.5f

        // Animar la entrada
        completionView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                // Animar la salida después de 1.5 segundos
                completionView.postDelayed({
                    completionView.animate()
                        .alpha(0f)
                        .scaleX(0.5f)
                        .scaleY(0.5f)
                        .setDuration(300)
                        .withEndAction {
                            // Eliminar la vista y cerrar el tutorial
                            binding.root.removeView(completionView)
                            closeTutorial()
                        }
                        .start()
                }, 1500)
            }
            .start()
    }

    private fun closeTutorial() {
        // Animar la salida del tutorial
        binding.root.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    parentFragmentManager.beginTransaction()
                        .remove(this@TutorialFragment)
                        .commit()
                }
            })
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class TutorialStep(
    val title: String,
    val description: String,
    val targetViewId: Int?
)
