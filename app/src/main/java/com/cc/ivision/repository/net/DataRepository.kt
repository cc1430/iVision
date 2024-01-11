package com.cc.ivision.repository.net


class DataRepository(private val network: NetworkHelper) {

    companion object {

        private var repository: DataRepository? = null

        fun getInstance(network: NetworkHelper): DataRepository {
            if (repository == null) {
                synchronized(DataRepository::class.java) {
                    if (repository == null) {
                        repository = DataRepository(network)
                    }
                }
            }

            return repository!!
        }
    }

    /**
     * 最新日报
     */
    /*suspend fun getDailyList() = requestDailyList()

    suspend fun requestDailyList() = withContext(Dispatchers.IO) {
        coroutineScope {
            val deferredDailyListResp = async { network.getDailyList() }
            val dailyListResp = deferredDailyListResp.await()
            dailyListResp
        }
    }*/

    /**
     * 推荐内容
     */
   /* suspend fun getRecommendData(recommendReqBody: RecommendReqBody) = requestRecommendData(recommendReqBody)

    private suspend fun requestRecommendData(recommendReqBody: RecommendReqBody) = withContext(Dispatchers.IO) {
        coroutineScope {
            val deferredRecommendReply = async { network.getRecommendData(recommendReqBody) }
            val recommendReply = deferredRecommendReply.await()
            recommendReply
        }
    }*/
}