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
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import com.woozoo.menumonya.databinding.DialogNoticeBinding

class NoticeDialog(context: Context): Dialog(context), OnClickListener {
    private lateinit var binding: DialogNoticeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // xml의 background 적용되도록 함.
        window?.setGravity(Gravity.CENTER) // 다이얼로그가 하단에 표시되도록 설정

        super.onCreate(savedInstanceState)
        binding = DialogNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)

        binding.noticeDescriptionTv.text = getSpannableText()
        binding.positiveBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.positive_btn -> {
                dismiss()
            }
        }
    }

    /**
     * 공지 다이얼로그의 강조하고자 하는 텍스트에 스타일 적용
     * - (주의) 텍스트 내용 수정시 setSpan()의 start, end 값도 수정해줘야 함.
     */
    private fun getSpannableText(): SpannableStringBuilder {
        val noticeString =
            context.resources.getString(R.string.notice_dialog_description)
        val spannable = SpannableStringBuilder(noticeString)

        val spans = listOf(
            ForegroundColorSpan(context.resources.getColor(R.color.colorPrimary)),
            UnderlineSpan(),
            StyleSpan(Typeface.BOLD)
        )

        for (span in spans) {
            spannable.setSpan(
                span,
                5,
                14,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }

        return spannable
    }
}