package com.woozoo.menumeonya

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.woozoo.menumeonya.databinding.ItemRestaurantBinding
import com.woozoo.menumeonya.model.Restaurant

class RestaurantAdapter(private val restaurantInfoArray: ArrayList<Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.ItemViewHolder>() {

    private lateinit var binding: ItemRestaurantBinding

    class ItemViewHolder(val binding: ItemRestaurantBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Restaurant) {
            binding.restaurantNameTv.text = data.name
            binding.restaurantPriceTv.text = data.price.cardPrice
            binding.restaurantTimeTv.text = data.time.openTime + " ~ " + data.time.closeTime
            binding.restaurantPhoneNumberTv.text = "02-3301-6148"
            binding.restaurantLocationDescriptionTv.text = data.location.description
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        binding = ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(restaurantInfoArray[position])
    }

    override fun getItemCount(): Int {
        return restaurantInfoArray.size
    }

}