package com.siri.travelguidemultiswipedbrealtime.module

import android.graphics.Canvas
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.siri.travelguidemultiswipedbrealtime.R
import com.siri.travelguidemultiswipedbrealtime.module.adapter.SwipeAdapter
import com.siri.travelguidemultiswipedbrealtime.data.model.PlaceModel
import com.siri.travelguidemultiswipedbrealtime.extension.loadImageByBase64
import com.siri.travelguidemultiswipedbrealtime.util.Logger

class ItemListAdapter(dataSet: List<PlaceModel> = emptyList())
    : SwipeAdapter<PlaceModel, ItemListAdapter.ViewHolder>(dataSet) {

    class ViewHolder(iceCreamLayout: View) : SwipeAdapter.ViewHolder(iceCreamLayout) {
        val reviewLocationNameView: TextView = itemView.findViewById(R.id.review_location_name)
        val reviewLocationDesView: TextView = itemView.findViewById(R.id.review_location_des)
        val travelPhotoView: ImageView = itemView.findViewById(R.id.img_photo)
        val dragIcon: AppCompatImageView = itemView.findViewById(R.id.drag_icon)
    }

    override fun getViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(item: PlaceModel, viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.itemView.context

        // Set place name and review
        viewHolder.reviewLocationNameView.text = item.name
        viewHolder.reviewLocationDesView.text = item.review
        // Set the image
        viewHolder.travelPhotoView.loadImageByBase64(item.imgLink)

    }

    override fun getViewToTouchToStartDraggingItem(item: PlaceModel, viewHolder: ViewHolder, position: Int) = viewHolder.dragIcon

    override fun onDragStarted(item: PlaceModel, viewHolder: ViewHolder) {
        Logger.log("Dragging started on ${item.name}")
    }

    override fun onSwipeStarted(item: PlaceModel, viewHolder: ViewHolder) {
        Logger.log("Swiping started on ${item.name}")
    }

    override fun onIsDragging(
        item: PlaceModel?,
        viewHolder: ViewHolder,
        offsetX: Int,
        offsetY: Int,
        canvasUnder: Canvas?,
        canvasOver: Canvas?,
        isUserControlled: Boolean) {
        // Call commented out to avoid saturating the log
        //Logger.log("The ${if (isUserControlled) "User" else "System"} is dragging ${item.name} (offset X: $offsetX, offset Y: $offsetY)")
    }

    override fun onIsSwiping(
        item: PlaceModel?,
        viewHolder: ViewHolder,
        offsetX: Int,
        offsetY: Int,
        canvasUnder: Canvas?,
        canvasOver: Canvas?,
        isUserControlled: Boolean) {
        // Call commented out to avoid saturating the log
        //Logger.log("The ${if (isUserControlled) "User" else "System"} is swiping ${item?.name} (offset X: $offsetX, offset Y: $offsetY)")
    }

    override fun onDragFinished(item: PlaceModel, viewHolder: ViewHolder) {
        Logger.log("Dragging finished on ${item.name} (the item was dropped)")
    }

    override fun onSwipeAnimationFinished(viewHolder: ViewHolder) {
        Logger.log("Swiping animation finished")
    }
}