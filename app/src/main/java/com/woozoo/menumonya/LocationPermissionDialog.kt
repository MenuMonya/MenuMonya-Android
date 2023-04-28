package com.woozoo.menumonya

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity.BOTTOM
import android.view.View
import android.view.View.OnClickListener
import com.woozoo.menumonya.databinding.DialogLocationPermissionBinding

class LocationPermissionDialog(context: Context) : Dialog(context), OnClickListener {
    private lateinit var binding: DialogLocationPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // xml의 background 적용되도록 함.
        window?.setGravity(BOTTOM)

        super.onCreate(savedInstanceState)
        binding = DialogLocationPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.negativeBtn.setOnClickListener(this)
        binding.positiveBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.negative_btn -> {
                dismiss()
            }
            R.id.positive_btn -> {

            }
        }
    }


}