package com.cc.ivision.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cc.ivision.bean.GestureData;
import com.cc.ivision.bean.GestureReqBean;
import com.cc.ivision.bean.GestureResp;
import com.cc.ivision.bean.ReqResp;
import com.cc.ivision.constant.Constant;
import com.cc.ivision.repository.NetworkHelper;
import com.cc.ivision.widget.FullScreenTextureView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class Camera2Helper implements Handler.Callback{

    private static final String TAG = "Camera2Helper";
    private Activity activity;
    private String mCameraId;                    //正在使用的相机id
    private FullScreenTextureView mTextureView;  // 预览使用的自定义TextureView控件
    private CameraCaptureSession mCaptureSession;// 预览用的获取会话
    private CameraDevice mCameraDevice;          // 正在使用的相机
    private static Range<Integer>[] fpsRanges;   // 相机的FPS范围

    private HandlerThread mBackgroundThread;     // 处理拍照等工作的子线程
    private Handler mBackgroundHandler;          // 上面定义的子线程的处理器
    private ImageReader mImageReader;            // 用于获取画面的数据，并进行识别

    private CaptureRequest.Builder mPreviewRequestBuilder;  // 预览请求构建器
    private CaptureRequest mPreviewRequest;      // 预览请求, 由上面的构建器构建出来
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);  // 信号量控制器
    CameraManager manager;
    private boolean hasFront = false;//是否有前置摄像头
    private boolean runClassifier = true;
    private Bitmap bitmapResult = null;

    // Camera2 API提供的最大预览宽度和高度
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private int realPreviewWidth;
    private int realPreviewHeight;
    private int outWidth;
    private int outHeight;
    private Size selectPreviewSize;              // 从相机支持的尺寸中选出来的最佳预览尺寸
    private Size mPreviewSize;                   // 预览数据的尺寸
    private Size outputPreviewSize;
    private int rotationDegrees;
    private CameraHelperRequest mCameraHelperRequest;
    String taskId;

    Boolean isFirstAdjustBody  = true;
    boolean isFirstGoing = true;
    long startTime =0;
    /**
     * 是否完成初始化
     */
    public volatile boolean hasInit = false;
    /**
     * 是否倒计时休息中
     */
    public volatile boolean isOnRest = false;
    /**
     * 是否校准身姿
     */
    public volatile boolean hasAdjust = false;
    /**
     * 是否开始评价
     */
    public volatile boolean hasStart = false;
    /**
     * 是否暂停
     */
    public volatile boolean hasStop = false;
    /**
     * 是否正在评价中
     */
    public volatile boolean isEvaluating = false;
    /**
     * 是否已弹暂停弹窗
     */
    public volatile boolean hasShow = false;
    /**
     * 是否已结束
     */
    public volatile boolean hasFinish = false;

    public volatile  boolean hasFinishPlaySound = true;
    /**
     * 区分是健身还是跳绳类型，basic-基础，skip-跳绳。缺省时默认为基础
     */
    private String type;
    /**
     * 区分跳绳类型，0---自由跳绳  1---定时跳绳  2---定数跳绳
     */
    private int skipType;

    public List<String> actionIdList = new ArrayList<>();
    public List<String> calorieList = new ArrayList<>();
    public List<String> segmentTimeList = new ArrayList<>();
    public List<String> urlList = new ArrayList<>();
    public int curPos=0;
    public Integer useCalorie = 0;

    public void setCurPos(int curPos) {
        this.curPos = curPos;
    }

    public void setCameraID(String cameraID) {
        this.mCameraId = cameraID;
    }




    public interface CameraHelperRequest {

        //void onImageAvailableResult(float[][] points, long costTime);
        void showImage(Bitmap bitmap);
        void showGesture(String gesture);
        void showBase64(String base64);

    }

    public void init(FullScreenTextureView textureView, String cameraID, int rotationDegrees
            , CameraHelperRequest cameraHelperRequest) {
        this.mTextureView = textureView;
        this.mCameraId= cameraID;
        this.rotationDegrees = rotationDegrees;
        this.mCameraHelperRequest = cameraHelperRequest;
        this.activity = (Activity) cameraHelperRequest;

        this.realPreviewWidth = Constant.PREVIEW_WIDTH;
        this.realPreviewHeight = Constant.PREVIEW_HEIGHT;
        this.outWidth = Constant.CAMERA_WIDTH;
        this.outHeight = Constant.CAMERA_HEIGHT;
    }

    public void init(FullScreenTextureView textureView, String cameraID,
                     int previewWidth, int previewHeight, int outWidth, int outHeight, int rotationDegrees
            , CameraHelperRequest cameraHelperRequest) {
        this.mTextureView = textureView;
        this.mCameraId= cameraID;
        this.realPreviewWidth = previewWidth;
        this.realPreviewHeight = previewHeight;
        this.outWidth = outWidth;
        this.outHeight = outHeight;
        this.rotationDegrees = rotationDegrees;
        this.mCameraHelperRequest = cameraHelperRequest;
        this.activity = (Activity)cameraHelperRequest;

    }

    public void setCameraHelperRequest(CameraHelperRequest cameraHelperRequest) {
        this.mCameraHelperRequest = cameraHelperRequest;
    }

    public void setPreviewWidth(int previewWidth) {
        this.realPreviewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        this.realPreviewHeight = previewHeight;
    }

    /**
     * 开启子线程
     */
    private void startBackgroundThread(Runnable runnable) {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper(), this);
        mBackgroundHandler.post(runnable);
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        if (message.what == 100) {//展示校准身姿进度框
            //mCameraHelperRequest.showAdjustBodyProgress();
            //mBackgroundHandler.sendEmptyMessageDelayed(101, 2000);
        }
        return false;
    }

    /**
     * Takes photos and classify them periodically.
     */
    private Runnable imageClassify = new Runnable() {
        @Override
        public void run() {
            if (false) {
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mBackgroundHandler.post(this);
            }
        }
    };

    /**
     * 停止子线程
     */
    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundHandler.removeCallbacks(imageClassify);
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (mCameraOpenCloseLock) {
                runClassifier = false;
            }
        }
    }

    /**
     * 开始
     */
    public void start() {
        synchronized (this) {

            runClassifier = true;
            startBackgroundThread(imageClassify);
            // 当屏幕关闭后重新打开, 若SurfaceTexture已经就绪, 此时onSurfaceTextureAvailable不会被回调, 这种情况下
            // 如果SurfaceTexture已经就绪, 则直接打开相机, 否则等待SurfaceTexture已经就绪的回调
            if (mTextureView.isAvailable()) {
                //Log.d(TAG, "mTextureView is Available~");
                openCamera();
            } else {
                //Log.d(TAG, "mTextureView mSurfaceTextureListener~");
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        }


    }

    /**
     * 暂停
     */
    public void pause() {
        synchronized (this) {
            if (mCaptureSession != null) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            stopBackgroundThread();
        }
    }

    /**
     * 结束
     */
    public void close() {
        synchronized (this) {
            closeCamera();
            stopBackgroundThread();
        }
    }


    /**
     * ImageReader的回调函数, 其中的onImageAvailable会以一定频率（由EXECUTION_FREQUENCY和相机帧率决定）
     * 识别从预览中传回的图像，并在透明的SurfaceView中画框
     */
    private final static int EXECUTION_FREQUENCY = 8;
    private int PREVIEW_RETURN_IMAGE_COUNT;
    int imageWidth;
    int imageHeight;
    long time00;
    byte[] bytes;
    long timeprocess04;
    int[] rgb;
    long beatTime = 0;
    String base64 = "";
    Boolean hasDetectSuccess = false;
    long successTime;

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG+"TIME", "输出时间" + System.currentTimeMillis());
            // 设置识别的频率，当EXECUTION_FREQUENCY为5时，也就是此处被回调五次只识别一次
            // 假若帧率被我设置在15帧/s，那么就是 1s 识别 3次，若是30帧/s，那就是1s识别6次，以此类推

            Image image = reader.acquireLatestImage();

            if (null == image) {
                return;
            }

            PREVIEW_RETURN_IMAGE_COUNT++;
            if(PREVIEW_RETURN_IMAGE_COUNT % EXECUTION_FREQUENCY !=0) {
                if (image != null) {
                    image.close();
                }
                return;
            }
            PREVIEW_RETURN_IMAGE_COUNT = 0;

            //time00 = System.currentTimeMillis();
            //Log.d(TAG, "ImageBeginss=" + time00);


            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
            Constant.CAMERA_WIDTH = imageWidth;
            Constant.CAMERA_HEIGHT = imageHeight;

            mCameraHelperRequest.showBase64("imageWidth=" + imageWidth + "  imageHeight=" + imageHeight + "  " + System.currentTimeMillis());

            Log.d(TAG + "OUT", "OUTPUT--> imageWidth=" + imageWidth
                    + "   imageHeight=" + imageHeight
                    + "   time="+System.currentTimeMillis());
            base64 = "";
            runClassifier = true;
            try {

                byte[] data68 = ImageUtil.INSTANCE.getDataFromImage(image, 2);

                int rgb[] = ImageUtil.INSTANCE.decodeYUV420SP(data68, imageWidth, imageHeight);
                Bitmap bitmap = Bitmap.createBitmap(rgb, 0, imageWidth,
                        imageWidth, imageHeight,
                        Bitmap.Config.ARGB_8888);
                String mFilePath="";
                try {
                    //File externalStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    File externalStorageDirectory = Environment.getExternalStorageDirectory();
                    final File dir = new File(externalStorageDirectory, "EyeChart");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    mFilePath = dir.getAbsolutePath() + "/eyechart_" + System.currentTimeMillis() + ".png";
                    File newFile = new File(mFilePath);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bos.flush();
                    bos.close();
                    bitmap.recycle();
                } catch (Exception e) {

                }

                if (image != null) {
                    image.close();
                }

                if (hasDetectSuccess) {
                    if (System.currentTimeMillis() - successTime < 3000) {
                        return;
                    } else {
                        hasDetectSuccess = false;
                    }

                }

                Log.d(TAG+"TIME", "data68 =  --------->"  + System.currentTimeMillis());
                base64 = "";
                File imageFile = new File(mFilePath);
                if (imageFile.exists()) {

                    //base64 = SignUtils.fileToBase64(mFilePath);

                    byte[] imgData = FileUtil.readFileByBytes(mFilePath);
                    String imgStr = Base64Util.encode(imgData);
                    base64 = URLEncoder.encode(imgStr, "UTF-8");

                    imageFile.delete();
                }
                /*if (bitmap != null) {
                    Log.i(TAG, "succeccful bitmap");
                    Matrix m = new Matrix();
                    m.postScale(-1, 1);  // 镜像水平翻转
                    Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

                    if(Build.VERSION.SDK_INT>=23) {
                        resultBitmap = ImageUtil.INSTANCE.adjustPhotoRotation(resultBitmap, 90);
                    }
                    //mCameraHelperRequest.showImage(resultBitmap);

                    base64 = getBitmapStrBase64(resultBitmap);

                    Log.d(TAG, "BAS464 =  " + base64);
                    //Jpeg_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, Jpegoutput);
                    //image_view.setImageBitmap(Jpeg_bitmap);
                    //Jpegoutput.flush();
                    //Jpegoutput.close();
                    //Log.i(TAG, "成功保存jpg文件");

                    Log.d(TAG+"TIME", "bitmap success =  --------->"  + System.currentTimeMillis());

                }*/



                mBackgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Log.d(TAG, "handler post  "  + System.currentTimeMillis() + "\nbase64 =  --------->" + base64);
                        if (!TextUtils.isEmpty(base64)) {
                            StringBuilder stringBuilder = new StringBuilder();
                            //FileUtils.writeTxtToFile(activity, base64);
                            Bitmap tempBitmap = base64ToBitmap(base64);
                            //base64 = getBitmapStrBase64(tempBitmap);
                            mCameraHelperRequest.showBase64(base64);
                            mCameraHelperRequest.showImage(tempBitmap);
                            //FileUtils.writeTxtToFile(activity, base64);
                            //gestureDetect(base64);
                            baiduGestureDetect(base64);

                        }

                    }
                });

            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    };


    /**
     *  打开相机
     *
     * 1. 获取相机权限
     * 2. 根据相机特性选取合适的Camera
     * 3. 通过CameraManager打开选择的相机
     */
    private void openCamera() {

        // 设置相机输出
        setUpCameraOutputs();

        if (TextUtils.isEmpty(mCameraId)) {
            return;
        }
        //Log.d(TAG, "manager  open Camera--->");
        // 获取CameraManager的实例
        manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            // 尝试获得相机开打关闭许可, 等待2500时间仍没有获得则排除异常
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            // 打开相机, 参数是: 相机id, 相机状态回调, 子线程处理器
            assert manager != null;
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * 设置相机的输出, 以支持全屏预览
     *
     * 处理流程如下:
     * 1. 获取当前的摄像头支持的输出map和帧率信息，并跳过前置摄像头
     * 2. 判断显示方向和摄像头传感器方向是否一致, 是否需要旋转画面
     * 3. 获取手机屏幕尺寸和相机的输出尺寸, 选择最合适的全屏预览尺寸
     * 4. 设置用于显示的TextureView和SurfaceView的尺寸，新建ImageReader对象
     */
    private void setUpCameraOutputs() {
        // 获取CameraManager实例
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            // 遍历运行本应用的设备的所有摄像头
            assert manager != null;
            boolean isFirst = true;
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    hasFront = true;
                    Log.d(TAG,"LENS_FACING_FRONT  cameraId = " + cameraId);
                } else {
                    Log.d(TAG,"LENS_FACING_BACK  cameraId = " + cameraId);
                }
            }
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {

                    if (hasFront) {
                        if (isFirst) {
                            Log.d(TAG,"USE  LENS_FACING_FRONT cameraId = " + cameraId);
                            isFirst = false;
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }

                } else {

                    if (!hasFront) {
                        if (isFirst) {
                            isFirst = false;
                            Log.d(TAG,"USE  LENS_FACING_BACK  cameraId = " + cameraId);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }

                // StreamConfigurationMap包含相机的可输出尺寸信息
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // 得到相机的帧率范围，可以在构建CaptureRequest的时候设置画面的帧率
                fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                Log.d(TAG, ": fpsRanges = " + Arrays.toString(fpsRanges));

                // 获取手机目前的旋转方向(横屏还是竖屏, 对于"自然"状态下高度大于宽度的设备来说横屏是ROTATION_90
                // 或者ROTATION_270,竖屏是ROTATION_0或者ROTATION_180)
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                Log.d(TAG, "displayRotation: " + displayRotation + "          sensorOritentation: " + sensorOrientation);   // displayRotation: 0

                switch (displayRotation) {
                    // ROTATION_0和ROTATION_18setUpCameraOutputs0都是竖屏只需做同样的处理操作
                    // 显示为竖屏时, 若传感器方向为90或者270, 则需要进行转换(标志位置true)
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (sensorOrientation == 90 || sensorOrientation == 270) {
                            Log.d(TAG, "swappedDimensions set true !");
                            swappedDimensions = true;
                        }
                        break;
                    // ROTATION_90和ROTATION_270都是横屏只需做同样的处理操作
                    // 显示为横屏时, 若传感器方向为0或者180, 则需要进行转换(标志位置true)
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (sensorOrientation == 0 || sensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                //mPreviewSize = getBestSize(swappedDimensions, map);
                mPreviewSize = new Size(Constant.PREVIEW_WIDTH, Constant.PREVIEW_HEIGHT);
                outputPreviewSize = new Size(Constant.CAMERA_WIDTH, Constant.CAMERA_HEIGHT);

                // 设置画框用的surfaceView的展示尺寸，也是TextureView的展示尺寸（因为是竖屏，所以宽度比高度小）
                //surfaceHolder.setFixedSize(mPreviewSize.getHeight(),mPreviewSize.getWidth());
                Log.d(TAG+"OUT", "视频展示 ---> Width:" + mPreviewSize.getWidth()  +"  Height:"+mPreviewSize.getHeight());
                mTextureView.setAspectRatio(mPreviewSize.getWidth(),mPreviewSize.getHeight());

                Log.d(TAG+"OUT", "图片输出 ---> Width:" + outputPreviewSize.getWidth()  +"  Height:"+outputPreviewSize.getHeight());
                // 输入相机的尺寸必须是相机支持的尺寸，这样画面才能不失真，TextureView输入相机的尺寸也是这个
                mImageReader = ImageReader.newInstance(outputPreviewSize.getWidth(), outputPreviewSize.getHeight(),
                        ImageFormat.YUV_420_888, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(   // 设置监听和后台线程处理器
                        mOnImageAvailableListener, mBackgroundHandler);

                this.mCameraId = cameraId;   // 获得当前相机的Id
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG, "setUpCameraOutputs: camera_erro");
        }
    }

    /**
     * 相机状态改变的回调函数
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // 当相机打开执行以下操作:
            mCameraOpenCloseLock.release();  // 1. 释放访问许可
            mCameraDevice = cameraDevice;   // 2. 将正在使用的相机指向将打开的相机
            createCameraPreviewSession();   // 3. 创建相机预览会话
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            // 当相机失去连接时执行以下操作:
            mCameraOpenCloseLock.release();   // 1. 释放访问许可
            cameraDevice.close();             // 2. 关闭相机
            mCameraDevice = null;             // 3. 将正在使用的相机指向null
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            // 当相机发生错误时执行以下操作:
            mCameraOpenCloseLock.release();      // 1. 释放访问许可
            cameraDevice.close();                // 2. 关闭相机
            mCameraDevice = null;                // 3, 将正在使用的相机指向null
            activity.finish();                            // 4. 结束当前Activity
        }
    };

    /**
     * 创建预览对话
     *
     * 1. 获取用于输出预览的surface
     * 2. CaptureRequestBuilder的基本配置
     * 3. 创建CaptureSession，等待回调
     * 4. 会话创建完成后，配置CaptureRequest为自动聚焦模式，并设为最高帧率输出
     * 5. 重复构建上述请求，以达到实时预览
     */
    private void createCameraPreviewSession() {
        try {
            // 获取用来预览的texture实例
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth() ,mPreviewSize.getHeight());  // 设置宽度和高度
            Surface surface = new Surface(texture);  // 用获取输出surface

            // 预览请求构建(创建适合相机预览窗口的请求：CameraDevice.TEMPLATE_PREVIEW字段)
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //不用预览***************************************
            //mPreviewRequestBuilder.addTarget(surface);  //请求捕获的目标surface
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());

            // 创建预览的捕获会话
            mCameraDevice.createCaptureSession(Arrays.asList(//surface,//不用预览，只取输出
                            mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        //一个会话的创建需要比较长的时间，当创建成功后就会执行onConfigured回调
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // 相机关闭时, 直接返回
                            if (null == mCameraDevice) {
                                return;
                            }

                            // 会话可行时, 将构建的会话赋给mCaptureSession
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // 自动对焦
                                //在该模式中，AF算法连续地修改镜头位置以尝试提供恒定对焦的图像流。
                                //聚焦行为应适合静止图像采集; 通常这意味着尽可能快地聚焦。
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 自动闪光：与ON一样，除了相机设备还控制相机的闪光灯组件，在低光照条件下启动它
                                //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                                // 设置预览帧率
                                //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,fpsRanges[0]);
                                CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
                                Range<Integer>[] fpsRange = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

                                if(fpsRange != null && fpsRange.length > 0) {
                                    Range<Integer> maxFps = fpsRange[0];
                                    for (Range<Integer> aFpsRange : fpsRange) {
                                        if (maxFps.getLower() * maxFps.getUpper() < aFpsRange.getLower() * aFpsRange.getUpper()) {
                                            maxFps = aFpsRange;
                                        }
                                    }
                                    Log.d(TAG, ": fpsRange maxFps = " + maxFps.toString() +  "    lower:"+ maxFps.getLower() + "upper:"+maxFps.getUpper());
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, maxFps);
                                }

                                // 构建上述的请求
                                //(CaptureRequest mPreviewRequest是请求捕获参数的集合，包括连续捕获的频率等)
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                // 重复进行上面构建的请求, 以便显示预览
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(activity, "createCaptureSession Failed", Toast.LENGTH_SHORT).show();
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭正在使用的相机
     */
    private void closeCamera() {
        try {
            if (mCameraOpenCloseLock != null) {
                // 获得相机开打关闭许可
                mCameraOpenCloseLock.acquire();
            }
            // 关闭捕获会话
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            // 关闭当前相机
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            // 关闭拍照处理器
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            if (null != mCameraOpenCloseLock) {
                // 释放相机开打关闭许可
                mCameraOpenCloseLock.release();
            }
        }
    }




    /**
     * 比较两个Size的大小（基于它们的area）
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    /**
     *  SurfaceTexture监听器
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: width="+width+", height="+height);
            try {
                openCamera();    // SurfaceTexture就绪后回调执行打开相机操作
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    /**
     * Bitmap 通过Base64 转换为字符串
     * @param bitmap
     * @return
     */
    private String getBitmapStrBase64(Bitmap bitmap){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos);
        byte[] bytes = bos.toByteArray();
        String string = Base64.encodeToString(bytes, Base64.NO_WRAP);
        return string;
    }

    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    /**
     * 平台技术中心手势识别
     * @param base64
     */
    public void gestureDetect(String base64) {

        JSONObject optionsObject = new JSONObject();
        try {
            optionsObject.put("app_key", Constant.SENSE_APP_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sign = SignUtils.encodeSign(Constant.SENSE_APP_KEY, Constant.SENSE_SECRET);

        GestureReqBean reqBean = new GestureReqBean(sign, Constant.SENSE_APP_KEY, base64);
        /*RequestBody keyBody = RequestBody.create(MediaType.parse("text/plain"), Constant.SENSE_APP_KEY);
        RequestBody signBody = RequestBody.create(MediaType.parse("text/plain"), sign);
        RequestBody baseBody = RequestBody.create(MediaType.parse("text/plain"), base64);
        RequestBody optionsBody = RequestBody.create(MediaType.parse("text/plain"), optionsObject.toString());*/

        NetworkHelper.getInstance().gestureDetect(base64)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ReqResp<GestureData>>() {
                    @Override
                    public void accept(ReqResp<GestureData> bean) throws Exception {
                        long timeEnd = System.currentTimeMillis();

                        try {
                            if (bean == null) {
                                Log.d(TAG, "detect ---->   bean  is  null");
                                //mCameraHelperRequest.showErrorMsg("数据为null");
                            } else {

                                Log.d(TAG, "GESTURE Detect---->  " + bean.getError_code() +"              "+ bean.toString());
                                if (bean.getSuccess()) {

                                    GestureData data = bean.getData();
                                    if (null != data && null != data.getResult() && null != data.getResult().getGesture()) {

                                        Log.d(TAG, "gesture = " + data.getResult().getGesture());
                                        /*Boolean isLeft = false;
                                        Boolean isRight = true;
                                        if(data.getResult().getHand_list() != null) {
                                            if (data.getResult().getHand_list().getLeft() != null)  isLeft = true;
                                            if (data.getResult().getHand_list().getRight() != null) isRight = true;
                                        }*/
                                        successTime = System.currentTimeMillis();
                                        hasDetectSuccess = true;

                                        mCameraHelperRequest.showGesture(data.getResult().getGesture());
                                    }



                                } else {
                                    Log.d(TAG, "  TOAST ---> error_code= " + bean.getError_code() + "  msg =  " + bean.getMsg());
                                    //mCameraHelperRequest.showErrorMsg(bean.getMsg());
                                }
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "异常：" +   e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        Log.d(TAG, "GESTURE DETECT---->  throwable  " +throwable.getMessage());
                    }
                });
        /*HttpApis.Builder.getSenseService().gestureDetect(reqBean)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ReqResp<GestureData>>() {
                    @Override
                    public void accept(ReqResp<GestureData> bean) throws Exception {
                        long timeEnd = System.currentTimeMillis();

                        try {
                            if (bean == null) {
                                Log.d(TAG, "detect ---->   bean  is  null");
                                //mCameraHelperRequest.showErrorMsg("数据为null");
                            } else {

                                Log.d(TAG, "GESTURE Detect---->  " + bean.getError_code() +"              "+ bean.toString());
                                if (bean.getSuccess()) {

                                    GestureData data = bean.getData();
                                    if (null != data && null != data.getResult() && null != data.getResult().getGesture()) {

                                        Log.d(TAG, "gesture = " + data.getResult().getGesture());
                                        *//*Boolean isLeft = false;
                                        Boolean isRight = true;
                                        if(data.getResult().getHand_list() != null) {
                                            if (data.getResult().getHand_list().getLeft() != null)  isLeft = true;
                                            if (data.getResult().getHand_list().getRight() != null) isRight = true;
                                        }*//*
                                        successTime = System.currentTimeMillis();
                                        hasDetectSuccess = true;

                                        mCameraHelperRequest.showGesture(data.getResult().getGesture());
                                    }



                                } else {
                                    Log.d(TAG, "  TOAST ---> error_code= " + bean.getError_code() + "  msg =  " + bean.getMsg());
                                    //mCameraHelperRequest.showErrorMsg(bean.getMsg());
                                }
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "异常：" +   e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        Log.d(TAG, "GESTURE DETECT---->  throwable  " +throwable.getMessage());
                    }
                });*/

    }

    /**
     * 百度手势识别
     * @param access_token
     * @param base64
     */
    private void baiduGestureDetect(String base64) {
        SharedPreferences sharedPreferences= activity.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        String access_token =sharedPreferences.getString(Constant.SP_TOKEN,"");
        NetworkHelper.getInstance().baiduGestureDetect(access_token, base64)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<GestureResp>() {
                    @Override
                    public void accept(GestureResp bean) throws Exception {
                        long timeEnd = System.currentTimeMillis();
                        //Log.d(TAG, "detect ---->   bean  is  null");
                        try {
                            if (bean == null) {
                                Log.d(TAG, "detect ---->   bean  is  null");
                                //mCameraHelperRequest.showErrorMsg("数据为null");
                            } else {
                                hasDetectSuccess = true;
                                mCameraHelperRequest.showGesture(bean.toString());
                                Log.d(TAG, "GESTURE Detect---->  " + bean.toString());
                                /*if (bean.getSuccess()) {

                                    GestureData data = bean.getData();
                                    if (null != data && null != data.getResult() && null != data.getResult().getGesture()) {

                                        Log.d(TAG, "gesture = " + data.getResult().getGesture());
                                        *//*Boolean isLeft = false;
                                        Boolean isRight = true;
                                        if(data.getResult().getHand_list() != null) {
                                            if (data.getResult().getHand_list().getLeft() != null)  isLeft = true;
                                            if (data.getResult().getHand_list().getRight() != null) isRight = true;
                                        }*//*
                                        successTime = System.currentTimeMillis();
                                        hasDetectSuccess = true;

                                        mCameraHelperRequest.showGesture(data.getResult().getGesture());
                                    }



                                } else {
                                    Log.d(TAG, "  TOAST ---> error_code= " + bean.getError_code() + "  msg =  " + bean.getMsg());
                                    //mCameraHelperRequest.showErrorMsg(bean.getMsg());
                                }*/
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "异常：" +   e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        Log.d(TAG, "GESTURE DETECT---->  throwable  " +throwable.getMessage());
                    }
                });
    }


    /*private Size getBestSize(boolean swappedDimensions,  StreamConfigurationMap map){

        Size mPreviewSize = null;

        // 获取当前的屏幕尺寸, 放到一个点对象里
        Point screenSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
        // 初始时将屏幕认为是横屏的
        int screenWidth = screenSize.y;  // 2029
        int screenHeight = screenSize.x; // 1080
        Log.d(TAG, "screenWidth = "+screenWidth+", screenHeight = "+screenHeight); // 2029 1080

        // swappedDimensions: (竖屏时true，横屏时false)
        Log.d(TAG, "swappedDimensions ------>  " + swappedDimensions);
        if (swappedDimensions) {
            screenWidth = screenSize.x;  // 1080
            screenHeight = screenSize.y; // 2029
        }
        // 尺寸太大时的极端处理
        if (screenWidth > MAX_PREVIEW_HEIGHT) screenWidth = MAX_PREVIEW_HEIGHT;
        if (screenHeight > MAX_PREVIEW_WIDTH) screenHeight = MAX_PREVIEW_WIDTH;

        Log.d(TAG, "尺寸太大时的极端处理 after adjust, screenWidth = "+screenWidth+", screenHeight = "+screenHeight); // 1080 1920

        // 自动计算出最适合的预览尺寸（实际从相机得到的尺寸，也是ImageReader的输入尺寸）
        // 第一个参数:map.getOutputSizes(SurfaceTexture.class)表示SurfaceTexture支持的尺寸List
        selectPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                screenWidth,screenHeight,swappedDimensions);

        // 这里返回的selectPreviewSize，要注意横竖屏的区分
        Log.d(TAG, "初步计算预览尺寸   selectPreviewSize.getWidth: " + selectPreviewSize.getWidth() + "      selectPreviewSize.getHeight: " + selectPreviewSize.getHeight());  // 1920



        // 横竖屏尺寸交换，以便后面设置各种surface统一代码
        if(swappedDimensions) {
            mPreviewSize = selectPreviewSize;
        } else {
            mPreviewSize = new Size(selectPreviewSize.getHeight(), selectPreviewSize.getWidth());
        }

        mPreviewSize = new Size(this.realPreviewWidth, this.realPreviewHeight);
        selectPreviewSize = mPreviewSize;

        Log.d(TAG, "横竖屏转换后   mPreviewSize.getWidth: " + mPreviewSize.getWidth() + "    mPreviewSize.getHeight: " + mPreviewSize.getHeight());  // 1920

        return mPreviewSize;

    }

    *//**
     * 计算出最适合全屏预览的尺寸
     * 原则是宽度和屏幕宽度相等，高度最接近屏幕高度
     *
     * @param choices           相机支持的尺寸list
     * @param screenWidth       屏幕宽度
     * @param screenHeight      屏幕高度
     * @return 最合适的预览尺寸
     *//*
    private static Size chooseOptimalSize(Size[] choices, int screenWidth, int screenHeight, boolean swappedDimensions) {
        List<Size> bigEnough = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        if(swappedDimensions){  // 竖屏
            for(Size option : choices){
                String str = "["+option.getWidth()+", "+option.getHeight()+"]";
                stringBuilder.append(str);
                if(option.getHeight() != screenWidth || option.getWidth() > screenHeight) continue;
                bigEnough.add(option);
            }
        } else{     // 横屏
            for(Size option : choices){
                String str = "["+option.getWidth()+", "+option.getHeight()+"]";
                stringBuilder.append(str);
                if(option.getWidth() != screenHeight || option.getHeight() > screenWidth) continue;
                bigEnough.add(option);
            }
        }
        Log.d(TAG, "chooseOptimalSize: "+ stringBuilder);

        if(bigEnough.size() > 0){
            return Collections.max(bigEnough, new CompareSizesByArea());
        }else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[choices.length/2];
        }
    }*/
}
