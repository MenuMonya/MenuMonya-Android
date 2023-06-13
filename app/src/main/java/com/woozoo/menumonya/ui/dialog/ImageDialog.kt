package com.woozoo.menumonya.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import com.woozoo.menumonya.R
import com.woozoo.menumonya.databinding.DialogImageBinding

class ImageDialog(context: Context): Dialog(context) {

    private lateinit var binding: DialogImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // xml의 background 적용되도록 함.
        window?.setGravity(Gravity.CENTER)

        super.onCreate(savedInstanceState)
        binding = DialogImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCancelable(true)

        binding.dialogIv.setImageResource(R.drawable.menu_report_info_image)
        binding.dialogIv.layoutParams.width = context.resources.getDimension(R.dimen.menu_report_info_image_width).toInt()
        binding.dialogIv.layoutParams.height = context.resources.getDimension(R.dimen.menu_report_info_image_height).toInt()
        binding.dialogIv.requestLayout()
    }
}