package dam.moviles.app_pricedetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dam.moviles.app_pricedetective.R

class CarouselAdapter(private val images: List<Int>) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carousel, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.carouselImage)

        fun bind(imageResId: Int) {
            imageView.setImageResource(imageResId)
        }
    }
} 