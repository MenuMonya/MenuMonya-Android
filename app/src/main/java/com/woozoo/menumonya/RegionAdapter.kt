package com.woozoo.menumonya

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.woozoo.menumonya.Application.Companion.context
import com.woozoo.menumonya.Constants.Companion.REGION_BUTTON_TYPE
import com.woozoo.menumonya.Constants.Companion.REGION_REPORT
import com.woozoo.menumonya.Constants.Companion.REGION_REPORT_TYPE
import com.woozoo.menumonya.databinding.ItemRegionBinding
import com.woozoo.menumonya.model.Region

class RegionAdapter(private var data: ArrayList<Region>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private lateinit var binding: ItemRegionBinding
    var tracker: SelectionTracker<String>? = null // 선택 여부를 추적(track)함.

    init {
        setHasStableIds(true)
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

    override fun getItemId(position: Int): Long = position.toLong()

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
            tracker?.let {
                (holder as RegionButtonViewHolder).bind(data[position], it.isSelected(data[position].name))
            }
        } else {
            (holder as RegionReportViewHolder).bind()
        }
    }

    inner class RegionReportViewHolder(private val binding: ItemRegionBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.regionTv.text = REGION_REPORT
        }
    }

    inner class RegionButtonViewHolder(private val binding: ItemRegionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Region, isSelected: Boolean) {
            binding.regionTv.text = data.name

            if (isSelected) {
                binding.regionLayout.background = AppCompatResources.getDrawable(context(),
                    R.drawable.selector_location_button_selected)
                binding.regionTv.setTextColor(getColor(context(), R.color.white))
            } else {
                binding.regionLayout.background = AppCompatResources.getDrawable(context(),
                    R.drawable.selector_location_button)
                binding.regionTv.setTextColor(getColor(context(), R.color.gray600))
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object: ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String = data[adapterPosition].name
                override fun inSelectionHotspot(e: MotionEvent): Boolean = true // 이게 있어야 클릭 이벤트 활성화됨.
            }
    }

    class RegionDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null && recyclerView.getChildViewHolder(view) is RegionButtonViewHolder) {
                return (recyclerView.getChildViewHolder(view) as RegionAdapter.RegionButtonViewHolder).getItemDetails()
            }
            return null
        }
    }

    class RegionKeyProvider(private val adapter: RegionAdapter): ItemKeyProvider<String>(SCOPE_CACHED) {
        override fun getKey(position: Int): String? {
            return adapter.data[position].name
        }

        override fun getPosition(key: String): Int {
            return adapter.data.indexOfFirst { it.name == key }
        }
    }
}