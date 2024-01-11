package com.cc.ivision.api

import com.cc.ivision.bean.AccessTokenResp
import com.cc.ivision.bean.GestureResp
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface ReqBaiduApi {

    /*@Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("oauth/2.0/token")
    suspend fun getAccessToken(@Body reqBody : AccessTokenReq) : AccessTokenResp?*/

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("oauth/2.0/token")
    suspend fun getAccessToken(@Query("client_id") client_id : String, @Query("client_secret") client_secret : String,
                               @Query("grant_type") grant_type : String, @Body reqBody : RequestBody) : AccessTokenResp?

    @POST("rest/2.0/image-classify/v1/gesture")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    suspend fun gesture(@Query("access_token") access_token : String, @Body reqBody : RequestBody) :GestureResp?

    @POST("rest/2.0/image-classify/v1/gesture")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    fun baiduGesture(@Query("access_token") access_token : String, @Body reqBody : RequestBody) : Observable<GestureResp?>
}