package com.cc.ivision.repository.net

class NetworkHelper {

    companion object {

        private var network: NetworkHelper? = null

        fun getInstance(): NetworkHelper {
            if (network == null) {
                synchronized(NetworkHelper::class.java) {
                    if (network == null) {
                        network = NetworkHelper()
                    }
                }
            }
            return network!!
        }
    }

    /**
     * 最新日报
     */
    //suspend fun getDailyList() = HttpApis.Builder.zhihuService?.getDailyList()


    /**
     * 获取首页列表
     */
    //suspend fun getRecommendData(recommendReqBody: RecommendReqBody) = HttpApis.Builder.recommendService?.getRecommendData(recommendReqBody)
}