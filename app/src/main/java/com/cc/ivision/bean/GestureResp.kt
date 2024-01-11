package com.cc.ivision.bean

data class GestureResp(val log_id : Long, val result_num : Int, val result : List<BaiduGestureBean>?) {
    data class BaiduGestureBean(val probability : Float,//": 0.9844077229499817,
                                val top : Int, //": 20,
                                val height: Int, // 156,
                                val classname: String,// "Face",目标所属类别，24种手势、other、face
                                val width : Int,//": 116,
                                val left: Int//: 173
    )
}
