package com.siri.travelguidemultiswipedbrealtime.module

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.siri.travelguidemultiswipedbrealtime.R
import com.siri.travelguidemultiswipedbrealtime.module.adapter.SwipeRecyclerView
import com.siri.travelguidemultiswipedbrealtime.base.BaseListFragment
import com.siri.travelguidemultiswipedbrealtime.common.currentListFragmentConfig


class TravelGridFragment : BaseListFragment() {

    private val numberOfColumns = 2
    override val fragmentLayoutId = R.layout.fragment_grid_list_item

    override fun setupListLayoutManager(list: SwipeRecyclerView) {
        // Set grid linear layout manager
        list.layoutManager = GridLayoutManager(activity, numberOfColumns)
    }

    override fun setupListOrientation(list: SwipeRecyclerView) {
        // It is necessary to set the orientation in code so the list can work correctly.
        // Horizontal swiping is specified because this grid list is vertically scrollable.
        // For horizontally scrollable grid lists, vertical swiping should be used instead.
        list.orientation = SwipeRecyclerView.ListOrientation.GRID_LIST_WITH_HORIZONTAL_SWIPING

        // We set this property to stop the grid list from drawing top dividers in the first row
        list.numOfColumnsPerRowInGridList = numberOfColumns
    }

    override fun setupListItemLayout(list: SwipeRecyclerView) {
        if (currentListFragmentConfig.isUsingStandardItemLayout)
            setStandardItemLayoutAndDivider(list)
        else
            setCardViewItemLayoutAndNoDivider(list)
    }

    private fun setStandardItemLayoutAndDivider(list: SwipeRecyclerView) {
        list.itemLayoutId = R.layout.item_list_grid
        list.dividerDrawableId = R.drawable.divider_grid_list
    }

    private fun setCardViewItemLayoutAndNoDivider(list: SwipeRecyclerView) {
        list.itemLayoutId = R.layout.item_list_grid_cardview
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
                list.behindSwipedItemIconMargin = resources.getDimension(R.dimen.dimen_normal)
            } else {
                // We set our custom layouts to be displayed behind swiped items
                list.behindSwipedItemLayoutId = R.layout.behind_swiped_grid_list
                list.behindSwipedItemSecondaryLayoutId = R.layout.behind_swiped_grid_list_secondary
            }
    }

    override fun setupFadeItemLayoutOnSwiping(list: SwipeRecyclerView) {
        list.reduceItemAlphaOnSwiping = currentListFragmentConfig.isUsingFadeOnSwipedItems
    }

    companion object {
        fun newInstance() = TravelGridFragment()
    }
}