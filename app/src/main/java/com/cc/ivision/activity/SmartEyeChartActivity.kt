package com.cc.ivision.activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.util.Log
import android.widget.ImageView
import androidx.activity.viewModels
import com.cc.base.ui.BaseActivity
import com.cc.base.utils.ToastUtil
import com.cc.ivision.App
import com.cc.ivision.R
import com.cc.ivision.bean.EventParamBean
import com.cc.ivision.constant.Constant.SP_NAME
import com.cc.ivision.constant.Constant.SP_TOKEN
import com.cc.ivision.databinding.ActivitySmartEyeChartBinding
import com.cc.ivision.utils.PhoneCamera2Helper
import com.cc.ivision.viewmodel.MainViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class SmartEyeChartActivity : BaseActivity<ActivitySmartEyeChartBinding>(),
    com.cc.ivision.utils.PhoneCamera2Helper.CameraHelperRequest {

    private lateinit var mCameraHelper: PhoneCamera2Helper
    private val cameraID = "0"

    /**
     * viewModel
     */
    private val viewModel by viewModels<MainViewModel>()

    private var currentGesture:String = "OK"
    private var lastGesture :String? = null
    var numb = 0

    val DIRECTION_LEFT = 1
    val DIRECTION_RIGHT = 2
    val DIRECTION_UP = 3
    val DIRECTION_DOWN = 4
    val MIN_DEGREE = 40
    val MAX_DEGREE = 52

    val STATE_BEFORE_DETECT = 0
    val STATE_DETECT_PREPARE = 1
    val STATE_DETECTING = 2

    var state: Int = STATE_BEFORE_DETECT
    var leftEyeDegree: String = ""
    var degree = MIN_DEGREE
    var direction = DIRECTION_LEFT
    var failTimes: Int = 0
    var successTimes: Int = 0
    var isLuoYan = true
    var isLeftEye = true

    var mediaPlayer: MediaPlayer? = null

    companion object {
        const val TAG = "chenchen"
        const val CONTENTID = "contentId"
        const val SPECIALNAME = "specialName"
        const val LIST = "list"
        const val IDS = "ids"
        const val NAMES = "names"
        const val IMAGES = "images"
        const val CALORIES = "calories"
        const val DISPLAY_IMAGES = "displayImages"
        const val START_POS = "startPos"
        const val SEGMENT_TIMES = "segmentTimeList"
        const val TYPE = "type"

        @JvmStatic
        fun startActivity(context: Activity) {

            val starter = Intent(context, SmartEyeChartActivity::class.java)
            context.startActivity(starter)

        }
    }

    override fun getViewBinding(): ActivitySmartEyeChartBinding {
        return ActivitySmartEyeChartBinding.inflate(layoutInflater)
    }

    override fun initCreate() {
        EventBus.getDefault().register(this)

        initCamera()

        mViewBinding.rlExit.setOnClickListener {
            finish()
        }

        mViewBinding.rlTestType.setOnClickListener {
            if (isLuoYan) {
                mViewBinding.ivTestType.setImageResource(R.mipmap.img_yanjing)
                mViewBinding.tvTestType.text = "戴镜视力"
                mViewBinding.tvTestTypeShow.text = "戴镜视力"
            } else {
                mViewBinding.ivTestType.setImageResource(R.mipmap.img_luoyan)
                mViewBinding.tvTestType.text = "裸眼视力"
                mViewBinding.tvTestTypeShow.text = "裸眼视力"
            }
            isLuoYan = !isLuoYan
        }

        App.isGestureDetectStarting = false
        playPrepareVoice()
    }

    private fun playPrepareVoice() {
        Log.d(TAG, "playPrepareVoice: ")
        mediaPlayer = MediaPlayer.create(this, R.raw.voice_prepare)
        mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                App.isGestureDetectStarting = true
                state = STATE_DETECT_PREPARE
                ToastUtil.shortShow("准备完成后，请比OK手势！")
            }
        })
        mediaPlayer?.start()
    }

    private fun playPromptVoice() {
        Log.d(TAG, "playPromptVoice: ")
        mediaPlayer = MediaPlayer.create(this, R.raw.voice_start)
        mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                App.isGestureDetectStarting = true
                state = STATE_DETECTING
                degree = MIN_DEGREE
                successTimes = 0
                failTimes = 0
                mViewBinding.tvSuccess.text = successTimes.toString()
                mViewBinding.tvFail.text = failTimes.toString()
                generateSymbol(degree)
            }
        })
        mediaPlayer?.start()
    }

    private fun playCorrectVoice() {
        Log.d(TAG, "playCorrectVoice: ")
        mediaPlayer = MediaPlayer.create(this, R.raw.voice_correct)
        mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                App.isGestureDetectStarting = true
                state = STATE_DETECTING
                ToastUtil.shortShow("回答正确")
                generateSymbol(degree)
            }
        })
        mediaPlayer?.start()
    }

    private fun playWrongVoice() {
        Log.d(TAG, "playWrongVoice: ")
        mediaPlayer = MediaPlayer.create(this, R.raw.voice_wrong)
        mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                App.isGestureDetectStarting = true
                state = STATE_DETECTING
                ToastUtil.shortShow("回答错误")
                generateSymbol(degree)
            }
        })
        mediaPlayer?.start()
    }

    private fun playNextEyeVoice() {
        Log.d(TAG, "playNextEyeVoice: ")
        mediaPlayer = MediaPlayer.create(this, R.raw.voice_next_eye)
        mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                App.isGestureDetectStarting = true
                state = STATE_DETECT_PREPARE
                ToastUtil.shortShow("准备测试右眼，请比OK手势")
            }
        })
        mediaPlayer?.start()
    }

    private fun playFinishVoice() {
        Log.d(TAG, "playFinishVoice: ")
        mediaPlayer = MediaPlayer.create(this, R.raw.voice_finish)
        mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                App.isGestureDetectStarting = false
                state = STATE_BEFORE_DETECT
            }
        })
        mediaPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
    }

    private fun initCamera() {
        mCameraHelper = com.cc.ivision.utils.PhoneCamera2Helper().apply {

            init(
                mViewBinding.mTextureView, cameraID,
                0, this@SmartEyeChartActivity
            )
        }

        mCameraHelper?.start()

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        mCameraHelper?.close()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCamEvent(eventMsg: EventParamBean) {
        val sharedPreferences: SharedPreferences =
            this@SmartEyeChartActivity.getSharedPreferences(SP_NAME, MODE_PRIVATE)
        val access_token = sharedPreferences.getString(SP_TOKEN, "")

        viewModel.baiduGesture(access_token!!, eventMsg.base64)
            .observe(this@SmartEyeChartActivity) { resp->
                resp?.let { it ->
                    Log.d(TAG, "onCamEvent: result = " + it.result.toString())
                    it.result?.forEach { itemBean ->
                        when(itemBean.classname) {
                            "Ok" -> {
                                if (state == STATE_DETECT_PREPARE) {
                                    App.isGestureDetectStarting = false
                                    playPromptVoice()
                                    ToastUtil.shortShow("准备完毕，要开始测试喽~")
                                }
                            }
                            "Thumb_up" -> {
                                //向上
                                if (state == STATE_DETECTING) {
                                    App.isGestureDetectStarting = false
                                    judgeResult(DIRECTION_UP)
                                }
                            }
                            "Thumb_down" -> {
                                //向下
                                if (state == STATE_DETECTING) {
                                    App.isGestureDetectStarting = false
                                    judgeResult(DIRECTION_DOWN)
                                }
                            }
                            "One" -> {
                                //向左
                                if (state == STATE_DETECTING) {
                                    App.isGestureDetectStarting = false
                                    judgeResult(DIRECTION_LEFT)
                                }
                            }
                            "Two" -> {
                                //向右
                                if (state == STATE_DETECTING) {
                                    App.isGestureDetectStarting = false
                                    judgeResult(DIRECTION_RIGHT)
                                }
                            }
                            "Five" -> {
                                //停止或返回
                                App.isGestureDetectStarting = false
                                ToastUtil.shortShow("即将退出...")
                                mViewBinding.rlExit.postDelayed({
                                    finish()
                                }, 2000)
                            }
                            "Eight" -> {
                                //看不清，过，不知道
                                if (state == STATE_DETECTING) {
                                    App.isGestureDetectStarting = false
                                    judgeResult(-1)
                                }
                            }

                        }
                    }

                }
            }
    }

    override fun showImage(bitmap: Bitmap?) {
        //展示图片帧
        if (null == bitmap) return

        runOnUiThread {
            //mViewBinding.imageView.setImageBitmap(bitmap)
        }
    }

    override fun showBase64(base64: String?) {
        runOnUiThread {
            //mViewBinding.tvInfo.text = base64
        }
    }

    override fun showGesture(gesture: String?) {
        //展示当前手势
        runOnUiThread {
            // mViewBinding.tvInfo.text = gesture
            /*when (gesture) {
                "land" -> {}
                "forward" -> {}
                "stop" -> {}
                "back" -> {}
                "up" -> {}
                "down" ->  {}
                "left" -> {}
                "right" ->  {}
                else -> {}
            }*/
        }

    }

    private fun generateSymbol(degree: Int) {
        direction = getRandomDirection()
        setImageDirection(direction, mViewBinding.ivSymbol)
        val ratio: Float = (MAX_DEGREE - degree  * 1.0F + 1.0F) / (MAX_DEGREE - MIN_DEGREE  * 1.0F)
        scaleImage(ratio, mViewBinding.ivSymbol)
        if (isLeftEye) {
            leftEyeDegree = (degree / 10.0f).toString()
            mViewBinding.tvTestResult.text = leftEyeDegree
        } else {
            val rightEyeDegree = (degree / 10.0f).toString()
            mViewBinding.tvTestResult.text = "$leftEyeDegree/$rightEyeDegree"
        }
    }

    private fun getRandomDirection(): Int {
        val random = Random()
        val randomNumber = random.nextInt(4) + 1
        println(randomNumber)
        return randomNumber
    }

    private fun scaleImage(scaleRatio: Float, imageView: ImageView) {
        imageView.scaleX = scaleRatio
        imageView.scaleY = scaleRatio
        imageView.pivotX = (imageView.width / 2).toFloat()
        imageView.pivotY = (imageView.height / 2).toFloat()
    }

    private fun setImageDirection(direction: Int, imageView: ImageView) {
        when (direction) {
            DIRECTION_LEFT -> imageView.setImageResource(R.mipmap.e_left)
            DIRECTION_RIGHT -> imageView.setImageResource(R.mipmap.e_right)
            DIRECTION_UP -> imageView.setImageResource(R.mipmap.e_up)
            DIRECTION_DOWN -> imageView.setImageResource(R.mipmap.e_down)
        }
    }

    private fun judgeResult(gestureDirection: Int) {
        if (gestureDirection == direction) {
            successTimes++
            if (successTimes == 2) {
                if (degree == MAX_DEGREE) {
                    if (isLeftEye) {
                        isLeftEye = false
                        mViewBinding.ivLeftEye.setImageResource(R.mipmap.close_eye)
                        mViewBinding.ivRightEye.setImageResource(R.mipmap.open_eye)
                        ToastUtil.shortShow("你的左眼视力" + degree / 10.0f)
                        playNextEyeVoice()
                    } else {
                        playFinishVoice()
                        ToastUtil.shortShow("你的右眼视力" + degree / 10.0f)
                    }
                    return
                }
                degree += 1
                successTimes = 0
                failTimes = 0
            }
            playCorrectVoice()
        } else {
            failTimes++
            if (failTimes == 2) {
                if (isLeftEye) {
                    isLeftEye = false
                    mViewBinding.ivLeftEye.setImageResource(R.mipmap.close_eye)
                    mViewBinding.ivRightEye.setImageResource(R.mipmap.open_eye)
                    ToastUtil.shortShow("你的左眼视力" + degree / 10.0f)
                    playNextEyeVoice()
                } else {
                    playFinishVoice()
                    ToastUtil.shortShow("你的右眼视力" + degree / 10.0f)
                }
            } else {
                playWrongVoice()
            }
        }
        mViewBinding.tvSuccess.text = successTimes.toString()
        mViewBinding.tvFail.text = failTimes.toString()
    }
}