package com.woozoo.menumonya

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.woozoo.menumonya.Constants.Companion.REGION_BUTTON_TYPE
import com.woozoo.menumonya.Constants.Companion.REGION_REPORT
import com.woozoo.menumonya.Constants.Companion.REGION_REPORT_TYPE
import com.woozoo.menumonya.databinding.ItemRegionBinding
import com.woozoo.menumonya.model.Region
import com.woozoo.menumonya.repository.RemoteConfigRepository
import com.woozoo.menumonya.util.AnalyticsUtils
import com.woozoo.menumonya.util.AnalyticsUtils.Companion.CONTENT_TYPE_REPORT_REGION_BUTTON

class RegionAdapter(private var data: ArrayList<Region>,
                    private val context: Context,
                    private val remoteConfigRepository: RemoteConfigRepository,
                    private val analyticsUtils: AnalyticsUtils)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    var selectedItemPos = -1
    var lastItemSelectedPos = -1

    private lateinit var binding: ItemRegionBinding

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

    // 이 부분을 꼭 구현해주어야 내가 설정한 뷰타입대로 적용된다.
    override fun getItemViewType(position: Int): Int {
        return if (data[position].name == REGION_REPORT) {
            REGION_REPORT_TYPE
        } else {
            REGION_BUTTON_TYPE
        }
    }

    /**
     * RecyclerView-selection을 활용한 RecyclerView의 아이템 클릭 이벤트 설정
     * (중요) Region의 name을 키 값으로 사용함.
     * (Ref) https://developer.android.com/guide/topics/ui/layout/recyclerview-custom?hl=ko#select
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (data[position].viewType == REGION_BUTTON_TYPE) {
            if(position == selectedItemPos)
                (holder as RegionButtonViewHolder).selectedBg()
            else
                (holder as RegionButtonViewHolder).defaultBg()
            holder.bind(data[position])
        } else {
            (holder as RegionReportViewHolder).bind()
        }
    }

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
    }

    inner class RegionButtonViewHolder(private val binding: ItemRegionBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                selectedItemPos = adapterPosition
                if (lastItemSelectedPos == -1)
                    lastItemSelectedPos = selectedItemPos
                else {
                    notifyItemChanged(lastItemSelectedPos)
                    lastItemSelectedPos = selectedItemPos
                }
                notifyItemChanged(selectedItemPos)
            }
        }

        fun bind(data: Region) {
            binding.regionTv.text = data.name
        }

        fun defaultBg() {
            binding.regionLayout.background = context.getDrawable(R.drawable.selector_location_button)
            binding.regionTv.setTextColor(ContextCompat.getColor(context, R.color.gray600))
        }

        fun selectedBg() {
            binding.regionLayout.background = context.getDrawable(R.drawable.selector_location_button_selected)
            binding.regionTv.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }
}