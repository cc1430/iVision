package com.cc.base.utils

import android.widget.Toast
import com.cc.base.BaseApplication

object ToastUtil {

    /**
     * 短提示
     * @param message 提示语
     */
    fun shortShow(message: String) {
        Toast.makeText(
            BaseApplication.context,
            message, Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * 长提示
     * @param message 提示语
     */
    fun longShow(message: String) {
        Toast.makeText(
            BaseApplication.context,
            message, Toast.LENGTH_LONG
        ).show()
    }

}