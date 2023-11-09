package com.siri.travelguidemultiswipedbrealtime.base

import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.siri.travelguidemultiswipedbrealtime.R
import com.siri.travelguidemultiswipedbrealtime.module.adapter.SwipeRecyclerView
import com.siri.travelguidemultiswipedbrealtime.module.ItemListAdapter
import com.siri.travelguidemultiswipedbrealtime.data.model.PlaceModel
import com.siri.travelguidemultiswipedbrealtime.listener.OnItemDragListener
import com.siri.travelguidemultiswipedbrealtime.listener.OnItemSwipeListener
import com.siri.travelguidemultiswipedbrealtime.listener.OnListScrollListener
import com.siri.travelguidemultiswipedbrealtime.util.Logger
import com.siri.travelguidemultiswipedbrealtime.datasource.PlaceTypeRepository
import com.siri.travelguidemultiswipedbrealtime.datasource.base.BaseRepositoryManagement

abstract class BaseListFragment : Fragment() {

    private val adapter = ItemListAdapter()
    private val repository = PlaceTypeRepository.getInstance()

    private lateinit var rootView: ViewGroup
    private lateinit var list: SwipeRecyclerView
    private lateinit var indicator: ProgressBar
    protected abstract val fragmentLayoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        rootView = inflater.inflate(fragmentLayoutId, container, false) as FrameLayout
        list = rootView.findViewById(R.id.list)
        indicator = rootView.findViewById(R.id.loading_indicator)
        list.adapter = adapter
        list.swipeListener = onItemSwipeListener
        list.dragListener = onItemDragListener
        list.scrollListener = onListScrollListener

        setupListLayoutManager(list)
        setupListOrientation(list)
        setupListItemLayout(list)
        setupLayoutBehindItemLayoutOnSwiping(list)
        setupFadeItemLayoutOnSwiping(list)

        return rootView
    }

    protected abstract fun setupListLayoutManager(list: SwipeRecyclerView)

    protected abstract fun setupListOrientation(list: SwipeRecyclerView)

    protected abstract fun setupListItemLayout(list: SwipeRecyclerView)

    protected abstract fun setupLayoutBehindItemLayoutOnSwiping(list: SwipeRecyclerView)

    protected abstract fun setupFadeItemLayoutOnSwiping(list: SwipeRecyclerView)


    private val onItemSwipeListener = object : OnItemSwipeListener<PlaceModel> {
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: PlaceModel): Boolean {
            when (direction) {
                OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT -> onItemSwipedLeft(item, position)
                OnItemSwipeListener.SwipeDirection.LEFT_TO_RIGHT -> onItemSwipedRight(item, position)
                OnItemSwipeListener.SwipeDirection.DOWN_TO_UP -> onItemSwipedUp(item, position)
                OnItemSwipeListener.SwipeDirection.UP_TO_DOWN -> onItemSwipedDown(item, position)
            }
            return false
        }
    }

    private val onItemDragListener = object : OnItemDragListener<PlaceModel> {
        override fun onItemDragged(previousPosition: Int, newPosition: Int, item: PlaceModel) {
            Logger.log("$item is being dragged from position $previousPosition to position $newPosition")
        }

        override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: PlaceModel) {
            if (initialPosition != finalPosition) {
                Logger.log("$item moved (dragged from position $initialPosition and dropped in position $finalPosition)")
                // Change item position inside the repository
                repository.removeItem(item)
                repository.insertItem(item, finalPosition)
            } else {
                Logger.log("$item dragged from (and also dropped in) the position $initialPosition")
            }
        }
    }

    private val onListScrollListener = object : OnListScrollListener {
        override fun onListScrollStateChanged(scrollState: OnListScrollListener.ScrollState) {
        }

        override fun onListScrolled(scrollDirection: OnListScrollListener.ScrollDirection, distance: Int) {
        }
    }

    private val onItemAddedListener = object : BaseRepositoryManagement.OnItemAdditionListener<PlaceModel> {
        override fun onItemAdded(item: PlaceModel, position: Int) {
            // Add the item to the adapter's data set if necessary
            if (!adapter.dataSet.contains(item)) {
                Logger.log("Added new item $item")
                adapter.insertItem(position, item)
                // We scroll to the position of the added item (positions match in both adapter and repository)
                list.smoothScrollToPosition(position)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // Unsubscribe from repository changes
        repository.removeOnItemAdditionListener(onItemAddedListener)
    }

    override fun onResume() {
        super.onResume()
        // Subscribe to repository changes and then reload items
        repository.addOnItemAdditionListener(onItemAddedListener)
        reloadItems()
    }

    private fun reloadItems() {
        // Show loader view
        indicator.visibility = View.VISIBLE
        list.visibility = View.GONE

        loadData()

        // Hide loader view after a small delay to simulate real data retrieval
        Handler().postDelayed({
            indicator.visibility = View.GONE
            list.visibility = View.VISIBLE
        }, 150)
    }

    private fun loadData() {
        adapter.dataSet = repository.getAllItems()
    }

    private fun onItemSwipedLeft(item: PlaceModel, position: Int) {
        Logger.log("$item (position $position) swiped to the left")

        removeItem(item, position)
    }

    private fun onItemSwipedRight(item: PlaceModel, position: Int) {
        Logger.log("$item (position $position) swiped to the right")

        archiveItem(item, position)
    }

    private fun onItemSwipedUp(item: PlaceModel, position: Int) {
        Logger.log("$item (position $position) swiped up")

        archiveItem(item, position)
    }

    private fun onItemSwipedDown(item: PlaceModel, position: Int) {
        Logger.log("$item (position $position) swiped down")

        removeItem(item, position)
    }

    private fun removeItem(item: PlaceModel, position: Int) {
        Logger.log("Removed item $item")
        removeItemFromList(item, position, R.string.itemRemovedMessage)
    }

    private fun archiveItem(item: PlaceModel, position: Int) {
        Logger.log("Archived item $item")
        removeItemFromList(item, position, R.string.itemArchivedMessage)
    }

    private fun removeItemFromList(item: PlaceModel, position: Int, stringResourceId: Int) {
        repository.removeItem(item)
        val itemSwipedSnackBar = Snackbar.make(rootView, getString(stringResourceId, item), Snackbar.LENGTH_SHORT)
        itemSwipedSnackBar.setAction(getString(R.string.undoCaps)) {
            Logger.log("UNDO: $item has been added back to the list in the position $position")
            repository.insertItem(item, position)
        }
        itemSwipedSnackBar.show()
    }
}