package com.woozoo.menumonya

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.woozoo.menumonya.Constants.Companion.GLIDE_IMAGE_SIZE_HEIGHT
import com.woozoo.menumonya.Constants.Companion.GLIDE_IMAGE_SIZE_WIDTH
import com.woozoo.menumonya.databinding.ItemRestaurantBinding
import com.woozoo.menumonya.model.Restaurant

class RestaurantAdapter(private val restaurantInfoArray: ArrayList<Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.ItemViewHolder>() {

    private lateinit var binding: ItemRestaurantBinding

    class ItemViewHolder(val binding: ItemRestaurantBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Restaurant) {
            binding.restaurantNameTv.text = data.name
            binding.restaurantPriceTv.text = data.price.cardPrice + "원"
            binding.restaurantTimeTv.text = data.time.openTime + " ~ " + data.time.closeTime
            binding.restaurantPhoneNumberTv.text = data.phoneNumber
            binding.restaurantLocationDescriptionTv.text = data.location.description

            Glide.with(binding.root)
                .load(data.imgUrl)
                .placeholder(R.drawable.restaurant_default_image)
                .override(GLIDE_IMAGE_SIZE_WIDTH, GLIDE_IMAGE_SIZE_HEIGHT)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.restaurantIv)
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