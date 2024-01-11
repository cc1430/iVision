package com.cc.ivision.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.cc.base.ui.BaseActivity
import com.cc.base.utils.ToastUtil
import com.cc.ivision.databinding.ActivityMainBinding
import com.cc.ivision.viewmodel.MainViewModel

class MainActivity : BaseActivity<ActivityMainBinding>() {

    var actionCode = -1
    var hasBound = false
    var mBinder : com.cc.ivision.aidl.ICamAidlInterface? = null
    private val mRequestCode = 100
    /**
     * viewModel
     */
    private val viewModel by viewModels<MainViewModel>()

    //需要申请两个权限，拍照和SD读写
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    var mPermissionlist = mutableListOf<String>()

    companion object {

        @JvmStatic
        fun startActivity(context : Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initCreate() {

        //获取token
        viewModel.getAccessToken().observe(this@MainActivity, { it ->
            it?.let {
                /**
                 * 若请求错误，服务器将返回的JSON文本包含以下参数：
                error： 错误码；关于错误码的详细信息请参考下方鉴权认证错误码。
                error_description： 错误描述信息，帮助理解和解决发生的错误。
                例如，认证失败返回：

                {
                "error": "invalid_client",
                "error_description": "unknown client id"
                }
                鉴权认证错误码

                error	error_description	解释
                invalid_client	unknown client id	API Key不正确
                invalid_client	Client authentication failed	Secret Key不正确
                 */
                if (TextUtils.isEmpty(it.error)) {
                    val sharedPreferences = getSharedPreferences(com.cc.ivision.constant.Constant.SP_NAME, MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString(com.cc.ivision.constant.Constant.SP_TOKEN, it.access_token)
                    editor.putString("expires_in", it.expires_in)

                    editor.commit()
                }
            }
        })

        if (Build.VERSION.SDK_INT >= 23) { //6.0才用动态权限
            initPermission()
        }

        mViewBinding.button2.setOnClickListener {

            actionCode = 1

            if (Build.VERSION.SDK_INT >= 23) { //6.0才用动态权限
                initPermission()
            } else {
                SmartEyeChartActivity.startActivity(this@MainActivity)
            }
        }

        mViewBinding.btnOpen.setOnClickListener {
            actionCode = 2
            if (Build.VERSION.SDK_INT >= 23) { //6.0才用动态权限
                initPermission()
            } else {
                // 绑定服务时自动创建服务
                com.cc.ivision.utils.CamManager.getInstance().init(this@MainActivity)
            }
        }

        mViewBinding.btnClose.setOnClickListener {
            com.cc.ivision.utils.CamManager.getInstance().stop()
        }
    }

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }


    //权限判断和申请
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initPermission() {
        mPermissionlist.clear() //清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (i in permissions.indices) {
            if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionlist.add(permissions[i]) //添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionlist.size > 0) { //有权限没有通过，需要申请
            requestPermissions(permissions, mRequestCode)
        } else {
            if (actionCode == 1) {
                SmartEyeChartActivity.startActivity(this@MainActivity)
            } else if (actionCode == 2) {
                com.cc.ivision.utils.CamManager.getInstance().stop()
            }
        }
    }


    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var hasPermissionDismiss = false //有权限没有通过
        if (mRequestCode == requestCode) {
            for (i in grantResults.indices) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showPermissionDialog() //跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
            } else {
                if (actionCode == 1) {
                    SmartEyeChartActivity.startActivity(this@MainActivity)
                } else if (actionCode == 2) {
                    com.cc.ivision.utils.CamManager.getInstance().stop()
                }
            }
        }
    }

    /**
     * 不再提示权限时的展示对话框
     */
    var mPermissionDialog: AlertDialog? = null

    private fun showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = AlertDialog.Builder(this)
                .setMessage("已禁用权限，请手动授予")
                .setCancelable(false)
                .setPositiveButton("设置") { dialog, which ->
                    cancelPermissionDialog()

                    //跳转到设置权限界面
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri =
                        Uri.fromParts("package", this.packageName, null)
                    intent.data = uri
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    //overridePendingTransition(0,0);
                }
                .setNegativeButton("取消") { dialog, which -> //关闭页面或者做其他操作
                    finish()
                }
                .create()
        }
        mPermissionDialog!!.show()
    }

    //关闭对话框
    private fun cancelPermissionDialog() {
        mPermissionDialog!!.cancel()
    }
}