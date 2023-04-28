package com.woozoo.menumonya

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.woozoo.menumonya.databinding.DialogMenuBinding
import com.woozoo.menumonya.model.Restaurant

class MenuDialog(context: Context, val data: Restaurant) : Dialog(context) {
    private lateinit var binding: DialogMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        super.onCreate(savedInstanceState)
        binding = DialogMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.restaurantMenuMainTv.text = data.todayMenu.main.replace(",", ", ")
        binding.restaurantMenuSideTv.text = data.todayMenu.side.replace(",", ", ")
        binding.restaurantMenuDessertTv.text = data.todayMenu.dessert.replace(",", ", ")
    }


}