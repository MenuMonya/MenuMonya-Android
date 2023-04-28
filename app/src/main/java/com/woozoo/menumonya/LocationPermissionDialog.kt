package com.woozoo.menumonya

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
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

        binding.permissionDescriptionTv.text = getSpannableText()

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

    /**
     * 위치 권한 다이얼로그의 '위치 서비스 사용' 텍스트에 스타일 적용
     * - (주의) 텍스트 내용 수정시 setSpan()의 start, end 값도 수정해줘야 함.
     */
    private fun getSpannableText(): SpannableStringBuilder {
        val locationPermissionString =
            context.resources.getString(R.string.location_permission_dialog_description)
        val spannable = SpannableStringBuilder(locationPermissionString)

        val spans = listOf(
            ForegroundColorSpan(context.resources.getColor(R.color.colorPrimary)),
            UnderlineSpan(),
            StyleSpan(Typeface.BOLD)
        )

        for (span in spans) {
            spannable.setSpan(
                span,
                14,
                23,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }

        return spannable
    }
}
