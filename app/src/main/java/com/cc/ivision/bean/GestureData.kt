package com.cc.ivision.bean

data class GestureData(val result:Result) {
    data class Result(val gesture:String?)//, val hand_list: HandList?)
    data class HandList(val Right:List<PointBean?>?, val Left:List<PointBean?>?)
    data class PointBean(val x:Float?, val y:Float?)
}
