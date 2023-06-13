package com.woozoo.menumonya.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.woozoo.menumonya.Constants.Companion.GLIDE_IMAGE_SIZE_HEIGHT
import com.woozoo.menumonya.Constants.Companion.GLIDE_IMAGE_SIZE_WIDTH
import com.woozoo.menumonya.R
import com.woozoo.menumonya.databinding.ItemRestaurantBinding
import com.woozoo.menumonya.data.model.Restaurant
import com.woozoo.menumonya.data.repository.RemoteConfigRepository
import com.woozoo.menumonya.ui.dialog.ImageDialog
import com.woozoo.menumonya.ui.dialog.MenuDialog
import com.woozoo.menumonya.util.AnalyticsUtils
import com.woozoo.menumonya.util.AnalyticsUtils.Companion.CONTENT_TYPE_REPORT_BUTTON
import com.woozoo.menumonya.util.DateUtils.Companion.getTodayDate
import com.woozoo.menumonya.util.DateUtils.Companion.getTodayMenuDateText

class RestaurantAdapter(private var restaurantInfoArray: ArrayList<Restaurant>,
                        private var buttonTextList: ArrayList<String>,
                        private val context: Context,
                        private val remoteConfigRepository: RemoteConfigRepository,
                        private val analyticsUtils: AnalyticsUtils
) : RecyclerView.Adapter<RestaurantAdapter.ItemViewHolder>() {

    private lateinit var binding: ItemRestaurantBinding

    class ItemViewHolder(val binding: ItemRestaurantBinding,
                         private val context: Context,
                         private val remoteConfigRepository: RemoteConfigRepository,
                         private val analyticsUtils: AnalyticsUtils,
                         private var buttonTextList: ArrayList<String>
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Restaurant) {
            binding.restaurantNameTv.text = data.name
            binding.restaurantPriceTv.text = data.price.cardPrice + "원"
            binding.restaurantTimeTv.text = data.time.openTime + " ~ " + data.time.closeTime
            binding.restaurantPhoneNumberTv.text = data.phoneNumber
            binding.restaurantLocationDescriptionTv.text = data.location.description
            binding.menuReportDescriptionTv.text = if (data.menuAvailableOnline) {
                context.resources.getString(R.string.restaurant_info_menu_report_description_online_available)
            } else {
                context.resources.getString(R.string.restaurant_info_menu_report_description)
            }

            if (data.todayMenu.date == getTodayDate()) { // 오늘 메뉴인 경우에만 표시함.
                // (1) 메뉴 레이아웃 표시
                binding.menuReportLayout.visibility = View.GONE
                binding.restaurantMenuLayout.visibility = View.VISIBLE
                binding.restaurantMenuMoreTv.visibility = View.VISIBLE
                binding.restaurantMenuMoreTv.setOnClickListener {
                    val menuDialog = MenuDialog(context, data)
                    menuDialog.show()
                }

                binding.menuDateTv.text = getTodayMenuDateText()
                if (data.todayMenu.provider != "") {
                    binding.menuProviderTv.text = String.format(
                        context.resources.getString(R.string.restaurant_info_menu_provider),
                        data.todayMenu.provider
                    )
                }

                binding.restaurantMenuMainTv.text = data.todayMenu.main.replace(",", ", ")
                binding.restaurantMenuSideTv.text = data.todayMenu.side.replace(",", ", ")
                binding.restaurantMenuDessertTv.text = data.todayMenu.dessert.replace(",", ", ")
            } else {
                // (2) 제보하기 레이아웃 표시
                binding.menuReportLayout.visibility = View.VISIBLE
                binding.restaurantMenuLayout.visibility = View.GONE
                binding.restaurantMenuMoreTv.visibility = View.GONE
                binding.menuReportBtn.text = buttonTextList.random()
                binding.menuReportBtn.setOnClickListener {
                    analyticsUtils.saveContentSelectionLog(CONTENT_TYPE_REPORT_BUTTON, data.name)

                    val menuReportUrl = remoteConfigRepository.getReportMenuUrlConfig()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(menuReportUrl))
                    context.startActivity(intent)
                }
                binding.menuReportInfoIv.setOnClickListener {
                    // TODO: 메뉴 수집 관련 정보 다이얼로그 표시
                    val imageDialog = ImageDialog(context)
                    imageDialog.show()
                }
            }

            Glide.with(binding.root)
                .load(data.imgUrl)
                .placeholder(R.drawable.restaurant_default_image)
                .override(GLIDE_IMAGE_SIZE_WIDTH, GLIDE_IMAGE_SIZE_HEIGHT)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.restaurantIv)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        binding = ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, context, remoteConfigRepository, analyticsUtils, buttonTextList)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(restaurantInfoArray[position])
    }

    override fun getItemCount(): Int {
        return restaurantInfoArray.size
    }

    fun setData(data: ArrayList<Restaurant>) {
        restaurantInfoArray = data
    }
}