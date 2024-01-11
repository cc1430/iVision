package com.cc.ivision.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.util.Log
import com.cc.ivision.aidl.ICamAidlInterface
import com.cc.ivision.utils.CamServiceHelper

class CamCenterService : Service(), CamServiceHelper.CameraHelperRequest {

    private var isOpen:Boolean = true
    private val cameraID = "0"
    private lateinit var mCameraHelper: CamServiceHelper

    inner class TaskReceiver: BroadcastReceiver() {

        override fun onReceive(p0: Context?, p1: Intent?) {

        }
    }

    override fun onCreate() {
        super.onCreate()

        Log.d("CameraHelper", "------> onCreate")

        mCameraHelper = CamServiceHelper().apply {
            init(cameraID, 0, this@CamCenterService)
        }
        mCameraHelper.start()

    }

    override fun onBind(intent: Intent): IBinder? {

        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()

        mCameraHelper?.close()
    }


    private val  mBinder = object : ICamAidlInterface.Stub() {
        override fun setSwitch(isOn: Boolean) {
            isOpen = isOn
        }

        override fun getSwitch(): Boolean {

            return isOpen
        }

        override fun basicTypes(
            anInt: Int,
            aLong: Long,
            aBoolean: Boolean,
            aFloat: Float,
            aDouble: Double,
            aString: String?
        ) {

        }

    }

    override fun showImage(bitmap: Bitmap?) {
        //图片帧输出
    }

    override fun showGesture(gesture: String?) {
        //展示当前判定的手势
    }
}