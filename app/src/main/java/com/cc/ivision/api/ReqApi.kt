package com.cc.ivision.api


import com.cc.ivision.bean.GestureData
import com.cc.ivision.bean.GestureReqBean
import com.cc.ivision.bean.ReqResp
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface ReqApi {

    @Multipart
    @POST("detect")
    fun detect(@Part("sign") sign: RequestBody, @Part("app_key") app_key: RequestBody,
               @Part("bases64") bases64: RequestBody, @Part("options") options: RequestBody
    ): Observable<ReqResp<GestureData?>?>


    @POST("detect")
    fun gestureDetect(@Body reqBody: GestureReqBean) : Observable<ReqResp<GestureData?>?>

}