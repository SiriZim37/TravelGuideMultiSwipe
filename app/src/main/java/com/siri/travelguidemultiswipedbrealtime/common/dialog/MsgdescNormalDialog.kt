package com.siri.travelguidemultiswipedbrealtime.common.dialog


import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import androidx.fragment.app.FragmentManager
import com.siri.travelguidemultiswipedbrealtime.R
import com.siri.travelguidemultiswipedbrealtime.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_normal.*
import java.io.ByteArrayOutputStream


class MsgdescNormalDialog(): BaseDialogFragment() {

    private var listenerActivity: Listener? = null
    private val PERMISSION_CODE = 1000;
    private val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null
    private var imageBitmap: Bitmap? = null


    private val listenerFragment by lazy {
        targetFragment?.let { it as Listener }
    }

    private val description by lazy {
        arguments?.getString(MESSAGE_DIALOG_EXTRA, "") ?: ""
    }

    private val confirmButtonMessage by lazy {
        arguments?.getString(CONFIRM_DIALOG_EXTRA, "") ?: ""
    }

    private val cancelButtonMessage by lazy {
        arguments?.getString(CANCEL_DIALOG_EXTRA, "") ?: ""
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_normal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initInstances()
    }

    private fun initInstances() {

        title.text = description
        btn_confirm.text = confirmButtonMessage
        btn_cancel.text = cancelButtonMessage

        if (confirmButtonMessage.isEmpty()) {
            btn_confirm.visibility = View.GONE
            divider.visibility = View.GONE
        }

        if (cancelButtonMessage.isEmpty()) {
            btn_cancel.visibility = View.GONE
            divider.visibility = View.GONE
        }

        btn_cancel.setOnClickListener {
            fragmentManager?.let { dismiss() }
            listenerActivity?.onDialogCancelClick()
            listenerFragment?.onDialogCancelClick()
        }

        addImg.setOnClickListener {  openCamera() }
//            img.loadImageByUrl(edt_img_url.text.toString())



        btn_confirm.setOnClickListener {
            fragmentManager?.let { dismiss() }
            listenerActivity?.onDialogConfirmClick(
                name =   edt_place_name.text.toString() ,
                review =  edt_review.text.toString(),
                imgUrl = setImageStr()
            )
            listenerFragment?.onDialogConfirmClick(
                name =   edt_review.text.toString(),
                review =  edt_review.text.toString(),
                imgUrl = setImageStr()
            )
        }
    }

    fun setImageStr() : String{
        var imgStr = Base64.encodeToString(compress(this!!.imageBitmap!!), Base64.DEFAULT)
        return imgStr
    }

    private fun compress(bitmap: Bitmap, quality: Int = 100): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }


    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listenerActivity = context as Listener
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "travelphoto")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }

        imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), image_uri)
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap!!, 800, 600, true);
        img.setImageURI(image_uri)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N ) {
            img.rotation = 90f
        }
    }



    interface Listener {
        fun onDialogConfirmClick( name : String , review : String , imgUrl : String)
        fun onDialogCancelClick()
    }

    companion object {
        private const val TAG = "MsgdescNormalDialog"
        private const val MESSAGE_DIALOG_EXTRA = "MESSAGE_DIALOG_EXTRA"
        private const val CONFIRM_DIALOG_EXTRA = "CONFIRM_DIALOG_EXTRA"
        private const val CANCEL_DIALOG_EXTRA = "CANCEL_DIALOG_EXTRA"

        fun newInstance() = MsgdescNormalDialog()

        fun show(fragmentManager: FragmentManager ,
                 description: String = "",
                 confirmButtonMessage: String = "",
                 cancelButtonMessage: String = "" ) {
            newInstance().apply {
                arguments = Bundle().apply {
                    putString(MESSAGE_DIALOG_EXTRA, description)
                    putString(CONFIRM_DIALOG_EXTRA, confirmButtonMessage)
                    putString(CANCEL_DIALOG_EXTRA, cancelButtonMessage)
                }
                show(fragmentManager, TAG)
            }
        }

    }
}