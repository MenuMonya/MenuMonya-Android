package com.woozoo.menumonya.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.woozoo.menumonya.Constants.Companion.REGION_BUTTON_TYPE
import com.woozoo.menumonya.Constants.Companion.REGION_REPORT
import com.woozoo.menumonya.Constants.Companion.REGION_REPORT_TYPE
import com.woozoo.menumonya.R
import com.woozoo.menumonya.databinding.ItemRegionBinding
import com.woozoo.menumonya.data.model.Region
import com.woozoo.menumonya.data.repository.RemoteConfigRepository
import com.woozoo.menumonya.util.AnalyticsUtils
import com.woozoo.menumonya.util.AnalyticsUtils.Companion.CONTENT_TYPE_REPORT_REGION_BUTTON

class RegionAdapter(private var data: ArrayList<Region>,
                    private val context: Context,
                    private val remoteConfigRepository: RemoteConfigRepository,
                    private val analyticsUtils: AnalyticsUtils)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    var selectedItemPosition = -1
    var lastItemSelectedPosition = -1

    val FIRST_ITEM_INDEX = 0
    var isFirstItemShowed = false // 최초 앱 실행시 첫번째 아이템(position이 0)을 활성화되어 보이도록 하기위한 flag
    var isFirstItemClicked = false

    private lateinit var binding: ItemRegionBinding
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = ItemRegionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return if (viewType == REGION_REPORT_TYPE) {
            RegionReportViewHolder(binding)
        } else {
            RegionButtonViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    // 이 부분을 꼭 구현해주어야 내가 설정한 뷰 타입대로 적용된다.
    override fun getItemViewType(position: Int): Int {
        return if (data[position].name == REGION_REPORT) {
            REGION_REPORT_TYPE
        } else {
            REGION_BUTTON_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (data[position].viewType == REGION_BUTTON_TYPE) {
            if (position == selectedItemPosition) {
                (holder as RegionButtonViewHolder).setSelectedBackground()
            } else {
                (holder as RegionButtonViewHolder).setDefaultBackground()

                // 최초 앱 실행시 첫번째 아이템(position이 0)을 활성화되어 보이도록 설정
                if (position == FIRST_ITEM_INDEX && !isFirstItemShowed) {
                    holder.setSelectedBackground()
                    isFirstItemShowed = true
                }
            }
            holder.bind(data[position])
        } else {
            (holder as RegionReportViewHolder).setReportButtonBackground()
            holder.bind()
        }
    }

    /**
     * '지역 건의' 버튼 ViewHolder
     */
    inner class RegionReportViewHolder(private val binding: ItemRegionBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.regionTv.text = REGION_REPORT
            binding.regionLayout.setOnClickListener {
                analyticsUtils.saveContentSelectionLog(CONTENT_TYPE_REPORT_REGION_BUTTON, CONTENT_TYPE_REPORT_REGION_BUTTON)

                val regionReportUrl = remoteConfigRepository.getRegionReportUrlConfig()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(regionReportUrl))
                context.startActivity(intent)
            }
        }

        /**
         * '지역건의' 버튼 디자인 적용
         */
        fun setReportButtonBackground() {
            binding.regionLayout.background = context.getDrawable(R.drawable.selector_location_report_button)
            binding.regionTv.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    /**
     * 지역 버튼 ViewHolder
     */
    inner class RegionButtonViewHolder(private val binding: ItemRegionBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // 지역 버튼 클릭시 이벤트
            itemView.setOnClickListener {
                val position = adapterPosition
                // 클릭 이벤트 리스너 적용
                if (position != RecyclerView.NO_POSITION) {
                    if (mListener != null) {
                        mListener.onItemClick(it, position)
                    }
                }

                // 클릭된 아이템이 하나만 표시되도록 설정
                selectedItemPosition = position
                if (lastItemSelectedPosition == -1) {
                    lastItemSelectedPosition = selectedItemPosition

                    /**
                     * 앱 최초 실행시 첫번재 아이템이 활성화 되어있고,
                     * 이후에 다른 아이템을 클릭하여 활성화된 경우 첫번째 아이템이 비활성화되지 않는 현상을 해결하기위한 코드
                     */
                    if (!isFirstItemClicked) {
                        notifyItemChanged(FIRST_ITEM_INDEX)
                        isFirstItemClicked = true
                    }
                } else {
                    notifyItemChanged(lastItemSelectedPosition)
                    lastItemSelectedPosition = selectedItemPosition
                }
                notifyItemChanged(selectedItemPosition)
            }
        }

        fun bind(data: Region) {
            binding.regionTv.text = data.name
        }

        fun setDefaultBackground() {
            binding.regionLayout.background = context.getDrawable(R.drawable.selector_location_button)
            binding.regionTv.setTextColor(ContextCompat.getColor(context, R.color.gray600))
        }

        fun setSelectedBackground() {
            binding.regionLayout.background = context.getDrawable(R.drawable.selector_location_button_selected)
            binding.regionTv.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }
}