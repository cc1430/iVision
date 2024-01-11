package com.cc.ivision.widget

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView

class FullScreenTextureView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    TextureView(context!!, attrs, defStyle) {
    private var mRatioWidth = 0
    private var mRatioHeight = 0
    /*private Matrix fullScreenMatrix = new Matrix();
    private Matrix defMatrix = null;

    @Override
    public void setTransform(Matrix transform) {
        if(defMatrix == null){
            defMatrix = transform;
        }
        super.setTransform(transform);
    }*/
    /**
     * 设置此视图的纵横比。 将基于从参数计算的比例来测量视图的大小。
     * mi 8上测试，这个比例是1280 / 960
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    fun setAspectRatio(width: Int, height: Int) {
        require(!(width < 0 || height < 0)) { "Size cannot be negative." }
        mRatioWidth = width
        mRatioHeight = height
        requestLayout()
    }

    /**
     * 设置为全屏TextureView
     * @param widthMeasureSpec 当前TextureView的宽
     * @param heightMeasureSpec 当前TextureView的高
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height)
        } else {
            //setMeasuredDimension(1080, 1920);
            //Log.d("Camera2BasicFragment", "mRatioWidth: "+mRatioWidth+", mRatioHeight: "+mRatioHeight);
            setMeasuredDimension(mRatioWidth, mRatioHeight)
        }
    }
}
