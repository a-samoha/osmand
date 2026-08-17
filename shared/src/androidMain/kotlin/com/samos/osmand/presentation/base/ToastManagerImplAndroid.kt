package com.samos.osmand.presentation.base

import android.content.Context
import android.widget.Toast

class ToastManagerImplAndroid(
    private val context: Context
) : ToastManager {

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
