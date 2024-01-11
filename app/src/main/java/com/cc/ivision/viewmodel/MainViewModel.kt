package com.cc.ivision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cc.base.network.HttpErrorDeal
import com.cc.ivision.BuildConfig
import com.cc.ivision.bean.AccessTokenResp
import com.cc.ivision.bean.GestureResp
import com.cc.ivision.bean.req.AccessTokenReq
import com.cc.ivision.repository.NetworkHelper
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class MainViewModel : ViewModel() {


    /**
     * 获取token
     */
    fun getAccessToken() = flow {
        val clientId : String = BuildConfig.APP_KEY//必须参数，应用的API Key
        val clientSecret : String = BuildConfig.APP_SECRET//必须参数，应用的Secret Key
        val req = AccessTokenReq(clientId, clientSecret)
        val data = NetworkHelper.getInstance().getAccessToken(clientId, clientSecret)
        emit(data)
    }.catch {
        if (it is Exception) {
            HttpErrorDeal.dealHttpError(it)
        }
        val resp = AccessTokenResp(access_token = "", refresh_token= null, expires_in= null,
        scope= null, invalid_client= null,error = "-1", error_description = "请求异常")
        emit(resp)
    }.asLiveData()

    /**
     * 获取token
     */
    fun baiduGesture(
        access_token : String,
        base64: String
    ) = flow {

        val data = NetworkHelper.getInstance().gesture(access_token, base64)
        emit(data)
    }.catch {
        if (it is Exception) {
            HttpErrorDeal.dealHttpError(it)
        }
        val resp = GestureResp(-1, -1, null)
        emit(resp)
    }.asLiveData()
}