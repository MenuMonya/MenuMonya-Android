package com.woozoo.menumonya

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.woozoo.menumonya.databinding.ItemRegionBinding
import com.woozoo.menumonya.model.Region

class RegionAdapter(private var data: ArrayList<Region>): RecyclerView.Adapter<RegionAdapter.RegionViewHolder>() {

    private lateinit var binding: ItemRegionBinding

    class RegionViewHolder(private val binding: ItemRegionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Region) {
            binding.regionTv.text = data.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionViewHolder {
        binding = ItemRegionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RegionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RegionViewHolder, position: Int) {
        holder.bind(data[position])
    }
}