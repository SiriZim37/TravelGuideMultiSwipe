package com.siri.travelguidemultiswipedbrealtime.module.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.view.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.siri.travelguidemultiswipedbrealtime.listener.OnItemDragListener
import com.siri.travelguidemultiswipedbrealtime.listener.OnItemSwipeListener
import com.siri.travelguidemultiswipedbrealtime.util.SwipeTouchHelper
import com.siri.travelguidemultiswipedbrealtime.util.drawHorizontalDividers
import com.siri.travelguidemultiswipedbrealtime.util.drawVerticalDividers
import kotlin.math.abs
import com.siri.travelguidemultiswipedbrealtime.module.adapter.SwipeRecyclerView.ListOrientation.DirectionFlag
import com.siri.travelguidemultiswipedbrealtime.module.adapter.SwipeRecyclerView.ListOrientation

abstract class SwipeAdapter<T, U : SwipeAdapter.ViewHolder>(
    dataSet: List<T> = emptyList()
) : RecyclerView.Adapter<U>() {

    private var recyclerView: SwipeRecyclerView? = null

    private var mutableDataSet: MutableList<T> = dataSet.toMutableList()
    var dataSet: List<T>
        get() = mutableDataSet
        set(value) {
            mutableDataSet = value.toMutableList()
            notifyDataSetChanged()
        }

    private val orientation: ListOrientation
        get() = recyclerView?.orientation
            ?: throw NullPointerException("The orientation of the SwipeRecyclerView is not defined.")


    abstract class ViewHolder(layout: View) : RecyclerView.ViewHolder(layout) {
        internal var canBeDragged: (() -> Boolean)? = null
        internal var canBeDroppedOver: (() -> Boolean)? = null
        internal var canBeSwiped: (() -> Boolean)? = null
        internal var isBeingDragged = false
        internal var isBeingSwiped = false
        internal var behindSwipedItemLayout: View? = null
        internal var behindSwipedItemSecondaryLayout: View? = null
    }

    protected abstract fun getViewHolder(itemView: View): U

    protected abstract fun onBindViewHolder(item: T, viewHolder: U, position: Int)

    protected abstract fun getViewToTouchToStartDraggingItem(item: T, viewHolder: U, position: Int): View?

    protected open fun canBeDragged(item: T, viewHolder: U, position: Int) = true

    protected open fun canBeDroppedOver(item: T, viewHolder: U, position: Int) = true

    protected open fun canBeSwiped(item: T, viewHolder: U, position: Int) = true

    protected open fun getBehindSwipedItemLayoutId(item: T, viewHolder: U, position: Int): Int? = null

    protected open fun getBehindSwipedItemSecondaryLayoutId(item: T, viewHolder: U, position: Int): Int? = null

    protected open fun onDragStarted(item: T, viewHolder: U) {
        // Do nothing in this method because it is up to the user of this library to implement or not
    }

    protected open fun onSwipeStarted(item: T, viewHolder: U) {
        // Do nothing in this method because it is up to the user of this library to implement or not
    }

    protected open fun onIsDragging(
        item: T?,
        viewHolder: U,
        offsetX: Int,
        offsetY: Int,
        canvasUnder: Canvas?,
        canvasOver: Canvas?,
        isUserControlled: Boolean) {
        // Do nothing in this method because it is up to the user of this library to implement or not
    }

    protected open fun onIsSwiping(
        item: T?,
        viewHolder: U,
        offsetX: Int,
        offsetY: Int,
        canvasUnder: Canvas?,
        canvasOver: Canvas?,
        isUserControlled: Boolean) {
        // Do nothing in this method because it is up to the user of this library to implement or not
    }

    protected open fun onDragFinished(item: T, viewHolder: U) {
        // Do nothing in this method because it is up to the user of this library to implement or not
    }

    protected open fun onSwipeAnimationFinished(viewHolder: U) {
        // Do nothing in this method because it is up to the user of this library to implement or not
    }

    private var itemTouchHelper: ItemTouchHelper
    private var dragListener: OnItemDragListener<T>? = null
    private var swipeListener: OnItemSwipeListener<T>? = null
    internal val swipeAndDragHelper: SwipeTouchHelper

    @Suppress("UNCHECKED_CAST")
    internal fun setInternalDragListener(listener: OnItemDragListener<*>?) {
        dragListener = listener as? OnItemDragListener<T>
    }

    @Suppress("UNCHECKED_CAST")
    internal fun setInternalSwipeListener(listener: OnItemSwipeListener<*>?) {
        swipeListener = listener as? OnItemSwipeListener<T>
    }

    private val itemDragListener = object : SwipeTouchHelper.OnItemDragListener {
        override fun onItemDragged(previousPosition: Int, newPosition: Int) {
            val item = mutableDataSet[previousPosition]
            onListItemDragged(previousPosition, newPosition)
            dragListener?.onItemDragged(previousPosition, newPosition, item)
        }

        override fun onItemDropped(initialPosition: Int, finalPosition: Int) {
            if (finalPosition == RecyclerView.NO_POSITION)
                return
            val item = mutableDataSet[finalPosition]
            dragListener?.onItemDropped(initialPosition, finalPosition, item)
        }
    }

    private val itemSwipeListener = object : SwipeTouchHelper.OnItemSwipeListener {
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection) {
            val item = mutableDataSet[position]
            if (swipeListener?.onItemSwiped(position, direction, item) != true)
                onListItemSwiped(position)
        }
    }

    private val stateChangeListener = object : SwipeTouchHelper.OnItemStateChangeListener {
        @Suppress("UNCHECKED_CAST")
        override fun onStateChanged(
            newState: SwipeTouchHelper.OnItemStateChangeListener.StateChangeType,
            viewHolder: RecyclerView.ViewHolder) {
            val dragDropSwipeViewHolder = viewHolder as U
            when (newState) {
                SwipeTouchHelper.OnItemStateChangeListener.StateChangeType.DRAG_STARTED ->
                    onDragStartedImpl(dragDropSwipeViewHolder)
                SwipeTouchHelper.OnItemStateChangeListener.StateChangeType.DRAG_FINISHED ->
                    onDragFinishedImpl(dragDropSwipeViewHolder)
                SwipeTouchHelper.OnItemStateChangeListener.StateChangeType.SWIPE_STARTED ->
                    onSwipeStartedImpl(dragDropSwipeViewHolder)
                SwipeTouchHelper.OnItemStateChangeListener.StateChangeType.SWIPE_FINISHED ->
                    onSwipeFinishedImpl(dragDropSwipeViewHolder)
            }
        }
    }

    private val itemLayoutPositionListener = object : SwipeTouchHelper.OnItemLayoutPositionChangeListener {
        @Suppress("UNCHECKED_CAST")
        override fun onPositionChanged(
            action: SwipeTouchHelper.OnItemLayoutPositionChangeListener.Action,
            viewHolder: RecyclerView.ViewHolder,
            offsetX: Int,
            offsetY: Int,
            canvasUnder: Canvas?,
            canvasOver: Canvas?,
            isUserControlled: Boolean) {

            val dragDropSwipeViewHolder = viewHolder as U
            when (action) {
                SwipeTouchHelper.OnItemLayoutPositionChangeListener.Action.SWIPING ->
                    onIsSwipingImpl(dragDropSwipeViewHolder, offsetX, offsetY, canvasUnder, canvasOver, isUserControlled)
                SwipeTouchHelper.OnItemLayoutPositionChangeListener.Action.DRAGGING ->
                    onIsDraggingImpl(dragDropSwipeViewHolder, offsetX, offsetY, canvasUnder, canvasOver, isUserControlled)
            }
        }
    }

    init {
        swipeAndDragHelper = SwipeTouchHelper(
            itemDragListener,
            itemSwipeListener,
            stateChangeListener,
            itemLayoutPositionListener,
            recyclerView)
        itemTouchHelper = ItemTouchHelper(swipeAndDragHelper)
    }

    override fun getItemCount() = mutableDataSet.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): U {
        val itemLayoutId = recyclerView?.itemLayoutId ?: 0
        if (itemLayoutId != 0) {
            val itemLayout = LayoutInflater.from(parent.context)
                .inflate(itemLayoutId, parent, false) as View
            return getViewHolder(itemLayout)
        } else throw NoSuchFieldException("Unless your adapter implements onCreateViewHolder(), " +
                "the attribute item_layout must be provided for the SwipeRecyclerView.")
    }

    override fun onBindViewHolder(holder: U, position: Int) {
        val item = mutableDataSet[position]

        holder.apply {
            // Setting these lambdas here instead of only once inside onCreateViewHolder() to make
            // sure they get set even if onCreateViewHolder() is overridden by the user
            canBeDragged = holder.canBeDragged ?: {
                val viewHolderPosition = holder.adapterPosition
                if (viewHolderPosition != RecyclerView.NO_POSITION) {
                    val viewHolderItem = mutableDataSet[viewHolderPosition]
                    canBeDragged(viewHolderItem, holder, viewHolderPosition)
                }
                else false
            }
            canBeDroppedOver = holder.canBeDroppedOver ?: {
                val viewHolderPosition = holder.adapterPosition
                if (viewHolderPosition != RecyclerView.NO_POSITION) {
                    val viewHolderItem = mutableDataSet[viewHolderPosition]
                    canBeDroppedOver(viewHolderItem, holder, viewHolderPosition)
                }
                else false
            }
            canBeSwiped = holder.canBeSwiped ?: {
                val viewHolderPosition = holder.adapterPosition
                if (viewHolderPosition != RecyclerView.NO_POSITION) {
                    val viewHolderItem = mutableDataSet[viewHolderPosition]
                    canBeSwiped(viewHolderItem, holder, viewHolderPosition)
                }
                else false
            }
            itemView.alpha = 1f
            behindSwipedItemLayout = getBehindSwipedItemLayout(item, holder, position)
            behindSwipedItemSecondaryLayout = getBehindSwipedItemSecondaryLayout(item, holder, position)
        }

        setViewForDragging(item, holder, position)

        onBindViewHolder(item, holder, position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        if (recyclerView is SwipeRecyclerView) {
            this.recyclerView = recyclerView
            itemTouchHelper.attachToRecyclerView(recyclerView)
            swipeAndDragHelper.recyclerView = recyclerView
        } else throw TypeCastException("The recycler view must be an extension of SwipeRecyclerView.")
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        if (recyclerView is SwipeRecyclerView) {
            this.recyclerView = null
            swipeAndDragHelper.recyclerView = null
        } else throw TypeCastException("The recycler view must be an extension of SwipeRecyclerView.")
    }

    fun addItem(item: T) {
        mutableDataSet.add(item)
        val position = mutableDataSet.indexOf(item)

        notifyItemInserted(position)
    }

    fun insertItem(position: Int, item: T) {
        mutableDataSet.add(position, item)

        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        mutableDataSet.removeAt(position)

        notifyItemRemoved(position)
    }

    fun moveItem(previousPosition: Int, newPosition: Int) {
        val item = mutableDataSet[previousPosition]
        mutableDataSet.removeAt(previousPosition)
        mutableDataSet.add(newPosition, item)

        notifyItemMoved(previousPosition, newPosition)
    }

    fun moveItem(newPosition: Int, item: T) {
        val previousPosition = mutableDataSet.indexOf(item)
        if (previousPosition != -1)
            moveItem(previousPosition, newPosition)
        else
            insertItem(newPosition, item)
    }

    private fun onListItemDragged(previousPosition: Int, newPosition: Int) {
        moveItem(previousPosition, newPosition)
    }

    private fun onListItemSwiped(position: Int) {
        removeItem(position)
    }

    private fun onDragStartedImpl(viewHolder: U) {
        viewHolder.isBeingDragged = true

        if (viewHolder.adapterPosition == RecyclerView.NO_POSITION)
            return

        val item = dataSet[viewHolder.adapterPosition]

        onDragStarted(item, viewHolder)
    }

    private fun onSwipeStartedImpl(viewHolder: U) {
        viewHolder.isBeingSwiped = true

        if (viewHolder.adapterPosition == RecyclerView.NO_POSITION)
            return

        val item = dataSet[viewHolder.adapterPosition]

        onSwipeStarted(item, viewHolder)
    }

    private fun onDragFinishedImpl(viewHolder: U) {
        viewHolder.isBeingDragged = false

        if (viewHolder.adapterPosition == RecyclerView.NO_POSITION)
            return

        val item = dataSet[viewHolder.adapterPosition]

        onDragFinished(item, viewHolder)
    }

    private fun onSwipeFinishedImpl(viewHolder: U) {
        viewHolder.isBeingSwiped = false

        onSwipeAnimationFinished(viewHolder)
    }

    private fun getBehindSwipedItemLayout(item: T, viewHolder: U, position: Int): View? {
        return getBehindSwipedItemLayoutId(item, viewHolder, position)?.let { behindSwipedItemLayoutId ->
            if (viewHolder.behindSwipedItemLayout?.id != behindSwipedItemLayoutId) {
                // The behind-swiped-items layout was never inflated before or the
                // layout ID has changed, so we inflate it
                return recyclerView?.context?.let { context ->
                    LayoutInflater
                        .from(context)
                        .inflate(behindSwipedItemLayoutId, null, false)
                }
            }

            // The behind-swiped layout was already inflated, so we just return it
            return viewHolder.behindSwipedItemLayout
        }
    }

    private fun getBehindSwipedItemSecondaryLayout(item: T, viewHolder: U, position: Int): View? {
        return getBehindSwipedItemSecondaryLayoutId(item, viewHolder, position)?.let { behindSwipedItemSecondaryLayoutId ->
            if (viewHolder.behindSwipedItemSecondaryLayout?.id != behindSwipedItemSecondaryLayoutId) {
                // The secondary behind-swiped-items layout was never inflated before
                // or the ID has changed, so we inflate it
                return recyclerView?.context?.let { context ->
                    LayoutInflater
                        .from(context)
                        .inflate(behindSwipedItemSecondaryLayoutId, null, false)
                }
            }

            // The secondary behind-swiped-items layout was already inflated, so we just return it
            return viewHolder.behindSwipedItemSecondaryLayout
        }
    }

    private fun onIsSwipingImpl(
        viewHolder: U,
        offsetX: Int,
        offsetY: Int,
        canvasUnder: Canvas?,
        canvasOver: Canvas?,
        isUserControlled: Boolean) {

        val currentAdapterPosition = viewHolder.adapterPosition
        val item = if (currentAdapterPosition != RecyclerView.NO_POSITION) dataSet[currentAdapterPosition] else null

        drawOnSwiping(offsetX, offsetY, viewHolder, canvasUnder, canvasOver)
        onIsSwiping(item, viewHolder, offsetX, offsetY, canvasUnder, canvasOver, isUserControlled)
    }

    private fun onIsDraggingImpl(
        viewHolder: U,
        offsetX: Int,
        offsetY: Int,
        canvasUnder: Canvas?,
        canvasOver: Canvas?,
        isUserControlled: Boolean) {

        val currentAdapterPosition = viewHolder.adapterPosition
        val item = if (currentAdapterPosition != RecyclerView.NO_POSITION) dataSet[currentAdapterPosition] else null

        drawOnDragging(canvasOver, viewHolder)
        onIsDragging(item, viewHolder, offsetX, offsetY, canvasUnder, canvasOver, isUserControlled)
    }

    private fun drawOnSwiping(
        offsetX: Int,
        offsetY: Int,
        viewHolder: U,
        canvasUnder: Canvas?,
        canvasOver: Canvas?) {

        recyclerView?.let { list ->

            val isSwipingHorizontally = (orientation.flagsswipeVal and SwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT.value == DirectionFlag.RIGHT.value)
                    || (orientation.flagsswipeVal and DirectionFlag.LEFT.value == DirectionFlag.LEFT.value)
            val isSecondaryDirection = (isSwipingHorizontally && offsetX > 0) || (!isSwipingHorizontally && offsetY < 0)

            // The current "coordinates" of the layout with the swipe translation applied to it
            val currentLayoutAreaLeft = viewHolder.itemView.left + viewHolder.itemView.translationX.toInt()
            val currentLayoutAreaTop = viewHolder.itemView.top + viewHolder.itemView.translationY.toInt()
            val currentLayoutAreaRight = viewHolder.itemView.right + viewHolder.itemView.translationX.toInt()
            val currentLayoutAreaBottom = viewHolder.itemView.bottom + viewHolder.itemView.translationY.toInt()

            // The original "coordinates" of the layout from which it is being moved away
            val originalLayoutAreaLeft = if (isSwipingHorizontally) viewHolder.itemView.left else currentLayoutAreaLeft
            val originalLayoutAreaTop = if (!isSwipingHorizontally) viewHolder.itemView.top else currentLayoutAreaTop
            val originalLayoutAreaRight = if (isSwipingHorizontally) viewHolder.itemView.right else currentLayoutAreaRight
            val originalLayoutAreaBottom = if (!isSwipingHorizontally) viewHolder.itemView.bottom else currentLayoutAreaBottom

            // If requested, make the item less opaque as it moves away from it original position
            var newItemAlpha = 1f
            if (list.reduceItemAlphaOnSwiping) {
                val offsetToItemSizeRatio =
                    if (isSwipingHorizontally)
                        abs(offsetX).toFloat() / (originalLayoutAreaRight - originalLayoutAreaLeft).toFloat()
                    else
                        abs(offsetY).toFloat() / (originalLayoutAreaBottom - originalLayoutAreaTop).toFloat()
                newItemAlpha = 1.1f - offsetToItemSizeRatio
                newItemAlpha = if (newItemAlpha < 0.1f) 0.1f else newItemAlpha
                newItemAlpha = if (newItemAlpha > 1f) 1f else newItemAlpha
                viewHolder.itemView.alpha = newItemAlpha
            }

            if (canvasUnder != null)
                drawLayoutBehindOnSwiping(
                    list,
                    canvasUnder,
                    viewHolder,
                    originalLayoutAreaLeft,
                    originalLayoutAreaTop,
                    originalLayoutAreaRight,
                    originalLayoutAreaBottom,
                    isSwipingHorizontally,
                    isSecondaryDirection)
            else if (canvasOver != null) {
                drawDividersOnSwiping(
                    list,
                    canvasOver,
                    viewHolder,
                    currentLayoutAreaLeft,
                    currentLayoutAreaTop,
                    currentLayoutAreaRight,
                    currentLayoutAreaBottom,
                    newItemAlpha,
                    originalLayoutAreaLeft,
                    originalLayoutAreaTop,
                    originalLayoutAreaRight,
                    originalLayoutAreaBottom)
            }
        }
    }

    private fun drawOnDragging(canvasOver: Canvas?, viewHolder: U) =
        recyclerView?.let { list ->
            if (canvasOver != null) drawDividers(list, canvasOver, viewHolder)
        }

    private fun drawLayoutBehindOnSwiping(
        list: SwipeRecyclerView,
        canvasUnder: Canvas,
        viewHolder: U,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        isSwipingHorizontally: Boolean,
        isSecondarySwipeDirection: Boolean) {

        canvasUnder.save()

        // Apply a clip rectangle with the calculated area to draw inside it
        canvasUnder.clipRect(left, top, right, bottom)

        // Get the custom layout to draw behind the swiped item, if any
        val behindLayoutMain = viewHolder.behindSwipedItemLayout
            ?: list.behindSwipedItemLayout
        val behindLayoutSecondary = viewHolder.behindSwipedItemSecondaryLayout
            ?: list.behindSwipedItemSecondaryLayout
        val behindLayout =
            if (isSecondarySwipeDirection && behindLayoutSecondary != null)
                behindLayoutSecondary
            else
                behindLayoutMain
        if (behindLayout != null) {

            val behindLayoutWidth = right - left
            val behindLayoutHeight = bottom - top

            // Draw the custom layout behind the item
            if (behindLayout.measuredWidth != behindLayoutWidth || behindLayout.measuredHeight != behindLayoutHeight)
                behindLayout.measure(
                    View.MeasureSpec.makeMeasureSpec(behindLayoutWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(behindLayoutHeight, View.MeasureSpec.EXACTLY))
            behindLayout.layout(left, top, right, bottom)
            canvasUnder.save()
            canvasUnder.translate(left.toFloat(), top.toFloat())
            behindLayout.draw(canvasUnder)

        } else {

            // Since there was no custom layout, look for the background color and draw it if necessary
            val backgroundColor =
                if (isSecondarySwipeDirection
                    && list.behindSwipedItemBackgroundSecondaryColor != null
                    && list.behindSwipedItemBackgroundSecondaryColor != Color.TRANSPARENT)
                    list.behindSwipedItemBackgroundSecondaryColor
                else
                    list.behindSwipedItemBackgroundColor
            if (backgroundColor != null && backgroundColor != Color.TRANSPARENT)
                canvasUnder.drawColor(backgroundColor)

            // Draw icon if necessary
            val iconBehindSwipedItem =
                if (isSecondarySwipeDirection && list.behindSwipedItemIconSecondaryDrawable != null)
                    list.behindSwipedItemIconSecondaryDrawable
                else
                    list.behindSwipedItemIconDrawable
            if (iconBehindSwipedItem != null) {

                // Calculate the icon position to be centered
                val iconWidth = iconBehindSwipedItem.intrinsicWidth
                val iconHeight = iconBehindSwipedItem.intrinsicHeight
                var iconCenterX = left + ((right - left) / 2)
                var iconCenterY = top + ((bottom - top) / 2)
                val halfIconWidth = iconWidth / 2
                val halfIconHeight = iconHeight / 2
                if (!list.behindSwipedItemCenterIcon) {

                    // Update the icon position to be near the side from which the swiping started
                    val margin = list.behindSwipedItemIconMargin.toInt()

                    if (isSwipingHorizontally && isSecondarySwipeDirection)
                        iconCenterX = left + margin + halfIconWidth
                    else if (isSwipingHorizontally && !isSecondarySwipeDirection)
                        iconCenterX = right - margin - halfIconWidth
                    else if (!isSwipingHorizontally && isSecondarySwipeDirection)
                        iconCenterY = bottom - margin - halfIconHeight
                    else if (!isSwipingHorizontally && !isSecondarySwipeDirection)
                        iconCenterY = top + margin + halfIconHeight
                }
                val iconLeft = iconCenterX - halfIconWidth
                val iconRight = iconLeft + iconWidth
                val iconTop = iconCenterY - halfIconHeight
                val iconBottom = iconTop + iconHeight
                iconBehindSwipedItem.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                // Finally, we draw the icon in the canvas
                iconBehindSwipedItem.draw(canvasUnder)
            }
        }

        canvasUnder.restore()
    }

    private fun drawDividersOnSwiping(
        list: SwipeRecyclerView,
        canvasOver: Canvas,
        viewHolder: U,
        currentLayoutAreaLeft: Int,
        currentLayoutAreaTop: Int,
        currentLayoutAreaRight: Int,
        currentLayoutAreaBottom: Int,
        newItemAlpha: Float,
        originalLayoutAreaLeft: Int,
        originalLayoutAreaTop: Int,
        originalLayoutAreaRight: Int,
        originalLayoutAreaBottom: Int) {
        // Draw dividers around the swiped item layout
        drawDividers(
            list,
            canvasOver,
            viewHolder,
            currentLayoutAreaLeft,
            currentLayoutAreaTop,
            currentLayoutAreaRight,
            currentLayoutAreaBottom,
            newItemAlpha)

        // Draw dividers around the area behind - except with grids (it looks odd with grids)
        if (orientation != SwipeRecyclerView.ListOrientation.GRID_LIST_WITH_HORIZONTAL_SWIPING
            && orientation != SwipeRecyclerView.ListOrientation.GRID_LIST_WITH_VERTICAL_SWIPING)
            drawDividers(
                list,
                canvasOver,
                viewHolder,
                originalLayoutAreaLeft,
                originalLayoutAreaTop,
                originalLayoutAreaRight,
                originalLayoutAreaBottom)
    }

    private fun drawDividers(
        list: SwipeRecyclerView,
        canvasOver: Canvas,
        viewHolder: U,
        left: Int? = null,
        top: Int? = null,
        right: Int? = null,
        bottom: Int? = null,
        alpha: Float? = null) {

        list.dividerDrawable?.let { dividerDrawable ->
            when (orientation) {
                SwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING,
                SwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_UNCONSTRAINED_DRAGGING ->
                    drawHorizontalDividers(viewHolder.itemView, canvasOver, dividerDrawable, left, right, alpha = alpha)

                SwipeRecyclerView.ListOrientation.HORIZONTAL_LIST_WITH_UNCONSTRAINED_DRAGGING,
                SwipeRecyclerView.ListOrientation.HORIZONTAL_LIST_WITH_HORIZONTAL_DRAGGING ->
                    drawVerticalDividers(viewHolder.itemView, canvasOver, dividerDrawable, top, bottom, alpha = alpha)

                SwipeRecyclerView.ListOrientation.GRID_LIST_WITH_HORIZONTAL_SWIPING,
                SwipeRecyclerView.ListOrientation.GRID_LIST_WITH_VERTICAL_SWIPING -> {
                    drawHorizontalDividers(viewHolder.itemView, canvasOver, dividerDrawable, left, right, alpha = alpha)
                    drawVerticalDividers(viewHolder.itemView, canvasOver, dividerDrawable, top, bottom, alpha = alpha)
                }
            }
        }
    }

    private fun setViewForDragging(item: T, holder: U, position: Int) {
        val viewToDrag = getViewToTouchToStartDraggingItem(item, holder, position) ?: holder.itemView
        if (recyclerView?.longPressToStartDragging != true)
            setItemDragAndDrop(viewToDrag, holder)
        else
            setItemDragAndDropWithLongPress(viewToDrag, holder)
    }

    private fun setItemDragAndDrop(viewToDrag: View, holder: U) =
        viewToDrag.setOnTouchListener { _, event ->
            if (holder.canBeDragged?.invoke() == true && event?.actionMasked == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(holder)
                return@setOnTouchListener true
            }
            false
        }

    private fun setItemDragAndDropWithLongPress(viewToDrag: View, holder: U) {
        val context = holder.itemView.context
        val longPressGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent) = !holder.isBeingSwiped && !holder.isBeingDragged
            override fun onLongPress(e: MotionEvent) = itemTouchHelper.startDrag(holder)
        }
        val longPressGestureDetector = GestureDetector(context, longPressGestureListener)
        longPressGestureDetector.setIsLongpressEnabled(true)

        viewToDrag.setOnTouchListener { _, event ->
            viewToDrag.onTouchEvent(event) // Pass the event up so current click listeners still work
            longPressGestureDetector.onTouchEvent(event)
        }
    }
}
