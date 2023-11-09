package com.siri.travelguidemultiswipedbrealtime.extension

import android.net.Uri
import androidx.annotation.DrawableRes
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.siri.travelguidemultiswipedbrealtime.R

fun ImageView.loadImageByUrl(url: String,
                             @DrawableRes placeholder: Int = R.drawable.bg_placeholder,
                             @DrawableRes error: Int = R.drawable.bg_placeholder) {
    url.ifEmpty { return }

    val request = RequestOptions()
            .placeholder(placeholder)
            .error(error)

    Glide.with(this)
            .load(url)
            .apply(request)
            .into(this)
}

fun ImageView.loadImageByBase64(base64: String,
                                         @DrawableRes placeholder: Int = R.drawable.bg_placeholder,
                                         @DrawableRes error: Int = R.drawable.bg_placeholder) {
    base64.ifEmpty { return }

    val request = RequestOptions()
            .placeholder(placeholder)
            .error(error)

    val imageByteArray = Base64.decode(base64, Base64.DEFAULT)

    Glide.with(this)
            .load(imageByteArray)
            .apply(request)
            .into(this)
}

fun ImageView.loadImageByDrawableRes(@DrawableRes drawableRes: Int) {
    Glide.with(this)
            .load(drawableRes)
            .into(this)
}

fun ImageView.loadImageByUri(uri: Uri?) {
    uri?.let {
        Glide.with(this)
                .load(it)
                .into(this)
    }
}
