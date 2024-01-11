package com.cc.ivision.bean

data class AccessTokenResp(val access_token : String? = null, val refresh_token : String? = null, val expires_in : String?= null,
val scope : String?= null, val invalid_client : String?= null, val error : String?= null, val error_description : String?= null)
