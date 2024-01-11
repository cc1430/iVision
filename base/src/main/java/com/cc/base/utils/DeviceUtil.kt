package com.cc.base.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

object DeviceUtil {

    /**
     * dp转为px
     * @param context  上下文
     * @param dipValue dp值
     * @return
     */
    fun dip2px(context: Context, dipValue: Float): Int {
        val r: Resources = context.applicationContext.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dipValue, r.displayMetrics
        ).toInt()
    }
}