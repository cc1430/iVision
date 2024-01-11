package com.cc.ivision.bean

import java.io.Serializable

data class ReqResp<T>(val success:Boolean, val error_code:String?, val msg: String? = null, val data: T? = null) : Serializable {

    override fun toString(): String {
        return "ReqResp(success=$success, error_code=$error_code, msg=$msg, data=$data)"
    }
}
