# TravelGuideMultiSwipe

# Example source code 
The application shows how to create SwipeRecyclerView to drag and drop card. To develop applications for recommending places, products, or others.

## Development Roadmap

- [x] [Kotlin](https://kotlinlang.org/)

## Features 

- [x] Recommended List
- [x] Add Place
- [ ] Map GeoLocation
- [ ] DB Realtime


### Introduction



In this article, we’re going to learn how to implement drag and drop reordering inside a recyclerView.

Here, we implement dragging and dropping to reorganise the items in the recyclerView; simply long-press an item to drag it and drop it in the desired location.

Here, we first create the recyclerview using static data, and then we use the ItemTouchHelper to implement the drag and drop functionality.

# Step 1
Create the fragment_grid_list_item.xml file of fragment file as shown below.


```xml
<!-- layout/view_layout.xml -->
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

   <com.siri.travelguidemultiswipedbrealtime.module.adapter.SwipeRecyclerView
        android:id="@+id/list"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:item_layout="@layout/list_item_layout"
        app:divider="@drawable/list_divider"/>

</FrameLayout>
```

# Step 2
Create the item_list.xml and item_list_cardview.xml  file in the layout folder for the recyclerview item. ( List with Card & without Card ) 

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    ... ">

    <androidx.appcompat.widget.AppCompatImageView
        android:id="@+id/ic_place_icon"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:layout_marginStart="@dimen/dimen_normal"
        android:layout_marginEnd="@dimen/dimen_large"
        android:layout_marginTop="0dp"
        android:layout_marginBottom="0dp"
        android:padding="0dp"
        android:src="@drawable/ic_add_item"/>

    <LinearLayout
        android:layout_width="0dp"
        android:layout_weight="1"
        android:layout_height="wrap_content"
        android:layout_marginTop="@dimen/dimen_smaller_x2"
        android:layout_marginBottom="@dimen/dimen_smaller_x2"
        android:orientation="vertical">

        <TextView
            android:id="@+id/review_location_name"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:textColor="#000000"
            tools:text="Location..."/>

        <TextView
            android:id="@+id/review_location_des"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="13sp"
            tools:text="Review"/>

    </LinearLayout>

    <androidx.appcompat.widget.AppCompatImageView
        android:id="@+id/drag_icon"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:paddingLeft="@dimen/dimen_normal"
        android:paddingRight="@dimen/dimen_normal"
        android:paddingStart="@dimen/dimen_normal"
        android:paddingEnd="@dimen/dimen_normal"
        android:src="@drawable/icon_drag"/>
</LinearLayout>
```
# Step 3
Create the ItemModel.kt class for item details that stores the item's name and its subdetails.

```kotlin
class PlaceModel(
    val type: String,
    val name: String,
    val review: String,
    val imgLink: String ) {
}
```

# Step 4

Create the ItemListAdapter.kt class as follows :

```kotlin

class ItemListAdapter(dataSet: List<PlaceModel> = emptyList()) {

    class ViewHolder(placeLayout: View) : SwipeAdapter.ViewHolder(placeLayout) {
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
     ...
}

```

# Step 5
Now implement the interface RecyclerRowMoveCallback.RecyclerViewRowTouchHelperContract in the SwipeAdapter class. So after that, your adapter files look like the following:


# Step 6

Create SwipeTouchHelper.kt and extends ItemTouchHelper.Callback as follows:

```kotlin

    override fun isLongPressDragEnabled() = false

    override fun isItemViewSwipeEnabled() = true

    ...
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (viewHolder is SwipeAdapter.ViewHolder) {
            return makeMovementFlags(
                    if (viewHolder.canBeDragged?.invoke() == true) mOrientation.flagsDragVal xor disabledDragFlagsValue else 0,
                    if (viewHolder.canBeSwiped?.invoke() == true) mOrientation.flagsswipeVal xor disabledSwipeFlagsValue else 0)
        }

        return 0
    }

    ...
    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder): Boolean {
        itemDragListener.onItemDragged(viewHolder.adapterPosition, target.adapterPosition)

        return true
    }

    ...
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (viewHolder != null) {
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> onStartedDragging(viewHolder)
                ItemTouchHelper.ACTION_STATE_SWIPE -> onStartedSwiping(viewHolder)
            }
        }
    }

    ...
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        onFinishedDraggingOrSwiping(viewHolder)
    }

    ...
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val swipeDirection =
                when (direction) {
                    ItemTouchHelper.LEFT -> SwipeDirection.RIGHT_TO_LEFT
                    ItemTouchHelper.RIGHT -> SwipeDirection.LEFT_TO_RIGHT
                    ItemTouchHelper.UP -> SwipeDirection.DOWN_TO_UP
                    else -> SwipeDirection.UP_TO_DOWN
                }

        itemSwipeListener.onItemSwiped(position, swipeDirection)
    }


    ...

    /**
     * Similar to the public interface of the library that has the same name, but for internal use only.
     * It will help pass the events from this class down to the adapter.
     */
    interface OnItemDragListener {
        fun onItemDragged(previousPosition: Int, newPosition: Int)
        fun onItemDropped(initialPosition: Int, finalPosition: Int)
    }

    interface OnItemSwipeListener {
        fun onItemSwiped(position: Int, direction: SwipeDirection)
    }

    interface OnItemStateChangeListener {

        enum class StateChangeType {
            DRAG_STARTED,
            DRAG_FINISHED,
            SWIPE_STARTED,
            SWIPE_FINISHED
        }

        fun onStateChanged(newState: StateChangeType, viewHolder: RecyclerView.ViewHolder) {
        }
    }

```

# Step 7

Create the BaseListFragment.kt to manage another sub Fragment class 

# Step 8

Create the PlaceTypeRepository.kt to add and implement BaseRepositoryManagement to generateNewItem

```kotlin

class PlaceTypeRepository : BaseRepositoryManagement<PlaceModel>() {

    override fun generateNewItem( placeType : String
                                  , placeName : String
                                  , review : String
                                  ,  imgUrl : String): PlaceModel {
        val place = PlaceModel( type = placeType,  name = placeName , review = review , imgLink = imgUrl )
        addItem(place)

        return place
    }
  ...
    companion object {
        private var instance: PlaceTypeRepository? = null

        fun getInstance(): PlaceTypeRepository {
            if (instance == null)
                instance =
                    PlaceTypeRepository()

            return instance as PlaceTypeRepository
        }
    }
}
```

## Example Demo  
![ezcv logo]( https://github.com/SiriZim37/TravelGuideMultiSwipe/blob/main/Art/multiSwipe.png)

