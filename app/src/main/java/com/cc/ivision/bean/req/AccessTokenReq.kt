package com.cc.ivision.bean.req

data class AccessTokenReq(val client_id : String, //必须参数，应用的API Key
                          val client_secret : String,//必须参数，应用的Secret Key
                          val grant_type : String? = "client_credentials"//必须参数，固定为client_credentials
)
