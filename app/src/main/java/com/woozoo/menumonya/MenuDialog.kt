package com.woozoo.menumonya

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.woozoo.menumonya.databinding.DialogMenuBinding
import com.woozoo.menumonya.model.Restaurant

class MenuDialog(context: Context, val data: Restaurant) : Dialog(context, R.style.custom_style_dialog) {
    private lateinit var binding: DialogMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // xml의 background 적용되도록 함.

        super.onCreate(savedInstanceState)
        binding = DialogMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setDialogWidthMatchParent()

        binding.restaurantMenuMainTv.text = data.todayMenu.main.replace(",", ", ")
        binding.restaurantMenuSideTv.text = data.todayMenu.side.replace(",", ", ")
        binding.restaurantMenuDessertTv.text = data.todayMenu.dessert.replace(",", ", ")
    }

    /**
     * (#26)
     * 커스텀 다이얼로그 특성상 xml에서 width, height에 MATCH_PARENT가 적용되지 않아 코드로 적용함.
     */
    fun setDialogWidthMatchParent() {
        val layoutParams = window?.attributes
        layoutParams?.width = MATCH_PARENT
    }
}