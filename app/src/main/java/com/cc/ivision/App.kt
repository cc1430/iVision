package com.cc.ivision

import com.cc.base.BaseApplication

class App: BaseApplication() {

    companion object {
        @JvmField
        var isGestureDetectStarting = false
    }

    override fun onCreate() {
        super.onCreate()
    }
}