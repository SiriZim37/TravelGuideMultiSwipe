package com.siri.travelguidemultiswipedbrealtime

import android.Manifest
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.siri.travelguidemultiswipedbrealtime.base.BaseListFragment
import com.siri.travelguidemultiswipedbrealtime.common.ListBottomFragmentType
import com.siri.travelguidemultiswipedbrealtime.common.currentBottomListFragmentType
import com.siri.travelguidemultiswipedbrealtime.common.dialog.MsgdescNormalDialog
import com.siri.travelguidemultiswipedbrealtime.datasource.PlaceTypeRepository
import com.siri.travelguidemultiswipedbrealtime.module.TravelGridFragment
import com.siri.travelguidemultiswipedbrealtime.module.TravelHorizontalFragment
import com.siri.travelguidemultiswipedbrealtime.module.LogFragment
import com.siri.travelguidemultiswipedbrealtime.util.Logger
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() , MsgdescNormalDialog.Listener {

    private var bottomNavigation: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.elevation = 0f

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.navigationBarColor = Color.BLACK

        initInstance()
        initViewModel()


        userGrantPermission(this) {}

    }

    fun initInstance(){
        setupLog()
        setupBottomNavigation()
        setupFab()
        refreshLogButtonText()
        navigateToListFragment()
    }

    fun initViewModel(){


    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (tryNavigateToListFragment(item.itemId))
            return@OnNavigationItemSelectedListener true
        false
    }

    private val onLogButtonClickedListener = View.OnClickListener {
        navigateToLogFragment()
    }

    private val onLogUpdatedListener = object: Logger.OnLogUpdateListener {
        override fun onLogUpdated() = refreshLogButtonText()
    }

    private val onFabClickedListener = View.OnClickListener {
        // When in the log fragment, the FAB clears the log; when in a list fragment, it adds an item
        if (isLogFragmentOpen())
            Logger.reset()
        else{
            MsgdescNormalDialog.show(
                fragmentManager = supportFragmentManager,
                description = "Add a recommended place",
                confirmButtonMessage = "Confirm",
                cancelButtonMessage = "Close"
            )
//            PlaceTypeRepository.getInstance().generateNewItem()
        }
    }

    private fun setupLog() {
        // Find log-related views
//        logButtonLayout = findViewById(R.id.see_log_button)
//        logButtonTextView = findViewById(R.id.see_log_button_text)

        // Initialise log and subscribe to log changes
        Logger.init(onLogUpdatedListener)

        // If the user clicks on the log button, we open the log fragment
        see_log_button.setOnClickListener(onLogButtonClickedListener)
    }

    private fun setupBottomNavigation() {
//        bottomNavigation = findViewById(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private fun setupFab() {
//        fab = findViewById(R.id.fab)
        fab.setOnClickListener(onFabClickedListener)
    }

    private fun refreshLogButtonText() {
        val numItemsOnLog = Logger.instance?.messages?.size ?: 0
        see_log_button_text.text = getString(R.string.seeLogMessagesTitle, numItemsOnLog)
    }

    private fun tryNavigateToListFragment(itemId: Int): Boolean {
        val listFragmentType: ListBottomFragmentType? = when (itemId) {
            R.id.navigation_horizontal_list -> ListBottomFragmentType.HORIZONTAL
            R.id.navigation_grid_list -> ListBottomFragmentType.GRID
            else -> null
        }

        if (listFragmentType != null && (listFragmentType != currentBottomListFragmentType || isLogFragmentOpen())) {
            navigateToListFragment(listFragmentType)

            return true
        }

        return false
    }

    private fun navigateToListFragment(listFragmentType: ListBottomFragmentType = currentBottomListFragmentType) {
        currentBottomListFragmentType = listFragmentType

        val fragment: BaseListFragment = when (listFragmentType) {
            ListBottomFragmentType.GRID -> TravelGridFragment.newInstance()
            ListBottomFragmentType.HORIZONTAL -> TravelHorizontalFragment.newInstance()
        }
        replaceFragment(fragment, listFragmentType.tag)
        onNavigatedToListFragment()
    }

    private fun navigateToLogFragment() {
        replaceFragment(LogFragment.newInstance(), LogFragment.TAG)
        onNavigatedToLogFragment()
    }

    private fun onNavigatedToListFragment() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(false)
        see_log_button.visibility = View.VISIBLE
        fab.setImageDrawable(AppCompatResources.getDrawable(applicationContext, R.drawable.ic_add_item))
    }

    private fun onNavigatedToLogFragment() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        see_log_button.visibility = View.GONE
        fab?.setImageDrawable(AppCompatResources.getDrawable(applicationContext, R.drawable.ic_delete_item))
    }

    private fun isLogFragmentOpen() = supportFragmentManager.findFragmentByTag(LogFragment.TAG) != null

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.content_frame, fragment, tag)
        }.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (isLogFragmentOpen()) {
                    navigateToListFragment()
                    return true
                }

                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (isLogFragmentOpen())
            navigateToListFragment()
        else
            super.onBackPressed()
    }


    override fun onDialogConfirmClick(name: String, review: String, imgBase64: String) {
        PlaceTypeRepository.getInstance().generateNewItem( "Travel" , name , review , imgBase64)
    }

    override fun onDialogCancelClick() {

    }

    private fun userGrantPermission(activity: Activity?, callback: () -> Unit) {
        Dexter.withContext(activity)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.isAnyPermissionPermanentlyDenied) {
                        return
                    }

                    callback.invoke()
                }

                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                    token.continuePermissionRequest()
                }
            }).check()
    }



}