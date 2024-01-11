package com.cc.ivision.repository

import com.cc.base.network.RetrofitServiceBuilder
import com.cc.base.network.BaseApi
import com.cc.ivision.api.ReqApi
import com.cc.ivision.api.ReqBaiduApi
import com.cc.ivision.bean.*
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject


class NetworkHelper {

    companion object {

        //创建service实例
        private var netWork = RetrofitServiceBuilder.createService(
            ReqApi::class.java
        )

        private var baiduApi = RetrofitServiceBuilder.createService(ReqBaiduApi::class.java, BaseApi.BAIDU_API)

        private var networkHelper: NetworkHelper? = null

        @JvmStatic
        fun getInstance(): NetworkHelper {
            if (networkHelper == null) {
                synchronized(NetworkHelper::class.java) {
                    if (networkHelper == null) {
                        networkHelper = NetworkHelper()
                    }
                }
            }
            return networkHelper!!
        }
    }

    /**
     * 平台技术中心  姿势检测
     * @param base64 图片信息
     */
    fun gestureDetect(
        base64: String
    ): Observable<ReqResp<GestureData?>?> {

        val optionsObject = JSONObject()
        try {
            optionsObject.put("app_key", com.cc.ivision.constant.Constant.SENSE_APP_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val sign = com.cc.ivision.utils.SignUtils.encodeSign(com.cc.ivision.constant.Constant.SENSE_APP_KEY, com.cc.ivision.constant.Constant.SENSE_SECRET)

        val reqBean = GestureReqBean(sign, com.cc.ivision.constant.Constant.SENSE_APP_KEY, base64)
        netWork!!.let {
            return it.gestureDetect(reqBean)
        }

    }


    /**
     * 获取token
     */
    suspend fun getAccessToken(client_id : String, client_secret : String,
                               grant_type : String? = "client_credentials") : AccessTokenResp? {
        val mediaType: MediaType? = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body: RequestBody = RequestBody.create(mediaType, "")

        baiduApi?.let {
            return it.getAccessToken(client_id, client_secret, grant_type!!, body)
        }
        return null
    }

    /**
     * 百度手势识别
     */
    fun baiduGestureDetect (
        access_token : String,
        base64: String
    ): Observable<GestureResp?> {


        val mediaType: MediaType? = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body: RequestBody = RequestBody.create(mediaType, "image=" + base64)

        baiduApi!!.let {
            return it.baiduGesture(access_token, body)
        }

    }

    /**
     * 协程百度手势
     */
    suspend fun gesture(
        access_token : String,
        base64: String
    ) : GestureResp? {
        val mediaType: MediaType? = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body: RequestBody = RequestBody.create(mediaType, "image=" + base64)

        baiduApi!!.let {
            return it.gesture(access_token, body)
        }
    }

}