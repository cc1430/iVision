package com.cc.ivision.utils;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.cc.ivision.constant.Constant;

import com.cc.ivision.aidl.ICamAidlInterface;

public class CamManager {

    private String TAG = "CamManager";
    private ICamAidlInterface mBinder;
    private Context mContext;
    private Boolean hasBound = false;

    static class CamManagerHolder {
        public static CamManager instance = new CamManager();
    }

    public static CamManager getInstance() {
        return CamManagerHolder.instance;
    }

    /**
     * 启动服务 只在Service中启动一次
     *
     * @param context
     */
    public void init(Context context) {
        this.mContext = context;
        if (mContext != null && mBinder == null)
        {
            connection();
        }
    }


    public ICamAidlInterface getCamManagerBinder() {
        return mBinder;
    }

    public ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(Constant.TAG, TAG + "onServiceConnected: <<<<<<<<<<<<<<<<<<<<<<<");
            hasBound = true;
            mBinder = ICamAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(Constant.TAG, TAG + "onServiceDisconnected: <<<<<<<<<<<<<<<<<<<<<<<");
            unConnection();
        }


    };

    /**
     * start Service
     */
    private void connection() {
        if (mBinder == null) {
            Log.i(TAG, "connection: startCamService<<<<<<<<<<<<<<<<<<<<<<<<< ");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.cc.ivision", "com.cc.ivision.CamCenterService"));
            mContext.bindService(intent, connection, Service.BIND_AUTO_CREATE);
        }
    }

    public void setSwitch(Boolean isOn) {
        if (null != mBinder) {
            try {
                mBinder.setSwitch(isOn);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean getSwitch() {
        if (null != mBinder) {
            try {
                return mBinder.getSwitch();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void stop(){
        unConnection();
    }

    private void unConnection() {
        if (mContext !=null && mBinder != null) {
            mContext.unbindService(connection);
            mBinder = null;
            hasBound = false;
        }
    }

}
