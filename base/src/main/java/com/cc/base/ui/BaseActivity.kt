package com.cc.base.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<T : ViewBinding> : FragmentActivity() {

    lateinit var mViewBinding: T

    protected open fun setStatusBar() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar()
        mViewBinding = getViewBinding()
        setContentView(mViewBinding.root)

        val displayMetrics = resources.displayMetrics
        displayMetrics.scaledDensity = displayMetrics.density
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val controller = ViewCompat.getWindowInsetsController(mViewBinding.root)
        controller?.isAppearanceLightNavigationBars = true
        controller?.isAppearanceLightStatusBars = true

        initCreate()

        com.cc.base.base.ViewManager.getInstance().addActivity(this)
    }

    abstract fun initCreate()

    abstract fun getViewBinding(): T


    override fun onDestroy() {
        super.onDestroy()
        com.cc.base.base.ViewManager.getInstance().finishActivity()
    }
}