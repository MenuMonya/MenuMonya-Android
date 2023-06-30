package com.woozoo.menumonya.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import com.woozoo.menumonya.R
import com.woozoo.menumonya.databinding.DialogReportBinding

class ReportDialog(context: Context, private val stringId: Int, private val listener: OnClickListener)
    : Dialog(context), OnClickListener {
    private lateinit var binding: DialogReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // xml의 background 적용되도록 함.
        window?.setGravity(Gravity.CENTER)

        super.onCreate(savedInstanceState)
        binding = DialogReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.permissionDescriptionTv.text = context.getString(stringId)

        binding.negativeBtn.setOnClickListener(this)
        binding.positiveBtn.setOnClickListener(listener)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.negative_btn -> {
                dismiss()
            }
        }
    }
}
