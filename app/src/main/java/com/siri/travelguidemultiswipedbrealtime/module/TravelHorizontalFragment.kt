package com.siri.travelguidemultiswipedbrealtime.module

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siri.travelguidemultiswipedbrealtime.R
import com.siri.travelguidemultiswipedbrealtime.module.adapter.SwipeRecyclerView
import com.siri.travelguidemultiswipedbrealtime.base.BaseListFragment
import com.siri.travelguidemultiswipedbrealtime.common.currentListFragmentConfig

class TravelHorizontalFragment : BaseListFragment() {

    override val fragmentLayoutId = R.layout.fragment_horizontal_list_item

    override fun setupListLayoutManager(list: SwipeRecyclerView) {
        // Set horizontal linear layout manager
        list.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
    }

    override fun setupListOrientation(list: SwipeRecyclerView) {
        // It is necessary to set the orientation in code so the list can work correctly
        list.orientation = if (currentListFragmentConfig.isRestrictingDraggingDirections)
            SwipeRecyclerView.ListOrientation.HORIZONTAL_LIST_WITH_HORIZONTAL_DRAGGING
        else
            SwipeRecyclerView.ListOrientation.HORIZONTAL_LIST_WITH_UNCONSTRAINED_DRAGGING
    }

    override fun setupListItemLayout(list: SwipeRecyclerView) {
        if (currentListFragmentConfig.isUsingStandardItemLayout)
            setStandardItemLayoutAndDivider(list)
        else
            setCardViewItemLayoutAndNoDivider(list)
    }

    private fun setStandardItemLayoutAndDivider(list: SwipeRecyclerView) {
        list.itemLayoutId = R.layout.item_list_horizontal
        list.dividerDrawableId = R.drawable.divider_horizontal_list
    }

    private fun setCardViewItemLayoutAndNoDivider(list: SwipeRecyclerView) {
        list.itemLayoutId = R.layout.item_list_horizontal_cardview
        list.dividerDrawableId = null
    }

    override fun setupLayoutBehindItemLayoutOnSwiping(list: SwipeRecyclerView) {
        // We set to null all the properties that can be used to display something behind swiped items
        list.behindSwipedItemBackgroundColor = null
        list.behindSwipedItemBackgroundSecondaryColor = null
        list.behindSwipedItemIconDrawableId = null
        list.behindSwipedItemIconSecondaryDrawableId = null
        list.behindSwipedItemLayoutId = null
        list.behindSwipedItemSecondaryLayoutId = null

        val currentContext = context
        if (currentListFragmentConfig.isDrawingBehindSwipedItems && currentContext != null)
            if (currentListFragmentConfig.isUsingStandardItemLayout) {
                // We set certain properties to show an icon and a background colour behind swiped items
                list.behindSwipedItemIconDrawableId = R.drawable.ic_delete_item
                list.behindSwipedItemIconSecondaryDrawableId = R.drawable.ic_archive_item
                list.behindSwipedItemBackgroundColor = ContextCompat.getColor(currentContext, R.color.swipeBehindRedBG)
                list.behindSwipedItemBackgroundSecondaryColor = ContextCompat.getColor(currentContext, R.color.swipeBehindSeconderyGreenBG)
                list.behindSwipedItemCenterIcon = true
            } else {
                // We set our custom layouts to be displayed behind swiped items
                list.behindSwipedItemLayoutId = R.layout.behind_swiped_horizontal_list
                list.behindSwipedItemSecondaryLayoutId = R.layout.behind_swiped_horizontal_list_secondary
            }
    }

    override fun setupFadeItemLayoutOnSwiping(list: SwipeRecyclerView) {
        list.reduceItemAlphaOnSwiping = currentListFragmentConfig.isUsingFadeOnSwipedItems
    }

    companion object {
        fun newInstance() = TravelHorizontalFragment()
    }
}