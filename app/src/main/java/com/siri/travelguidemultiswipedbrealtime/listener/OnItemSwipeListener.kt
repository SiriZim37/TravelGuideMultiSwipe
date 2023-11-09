package com.siri.travelguidemultiswipedbrealtime.listener

/** Listener for the swiping events of list items. */

interface OnItemSwipeListener<T> {

    /************** Indicates the direction in which the swipe action is performed.*************/

    enum class SwipeDirection {
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        DOWN_TO_UP,
        UP_TO_DOWN
    }

    /*
     * Callback for whenever an item has been swiped.
     * @param position The position of the swiped item.
     * @param direction The direction in which the item has been swiped.
     * @param item The item that has been swiped.
     * @return True if the event was handled (i.e., if the necessary changes were applied to the
     * adapter's data set within the callback); false otherwise. If false, the swiped item will be
     * removed from the adapter's data set automatically.
     */
    fun onItemSwiped(position: Int, direction: SwipeDirection, item: T): Boolean
}