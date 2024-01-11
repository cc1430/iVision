package com.cc.ivision.utils

import android.graphics.*
import android.media.Image
import android.util.Log
import java.io.ByteArrayOutputStream


object ImageUtil {
    private const val COLOR_FormatI420 = 1
    private const val COLOR_FormatNV21 = 2
    private fun isImageFormatSupported(image: Image): Boolean {
        val format = image.format
        when (format) {
            ImageFormat.YUV_420_888, ImageFormat.NV21, ImageFormat.YV12 -> return true
        }
        return false
    }

    fun getDataFromImage(image: Image, colorFormat: Int): ByteArray {
        require(!(colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21)) { "only support COLOR_FormatI420 " + "and COLOR_FormatNV21" }
        if (!isImageFormatSupported(image)) {
            throw RuntimeException("can't convert Image to byte array, format " + image.format)
        }
        val crop = image.cropRect
        val format = image.format
        val width = crop.width()
        val height = crop.height()
        val planes = image.planes
        val data = ByteArray(width * height * ImageFormat.getBitsPerPixel(format) / 8)
        val rowData = ByteArray(planes[0].rowStride)
        //if (VERBOSE) Log.v(TAG, "get data from " + planes.length + " planes");
        var channelOffset = 0
        var outputStride = 1
        for (i in planes.indices) {
            when (i) {
                0 -> {
                    channelOffset = 0
                    outputStride = 1
                }
                1 -> if (colorFormat == COLOR_FormatI420) {
                    channelOffset = width * height
                    outputStride = 1
                } else if (colorFormat == COLOR_FormatNV21) {
                    channelOffset = width * height + 1
                    outputStride = 2
                }
                2 -> if (colorFormat == COLOR_FormatI420) {
                    channelOffset = (width * height * 1.25).toInt()
                    outputStride = 1
                } else if (colorFormat == COLOR_FormatNV21) {
                    channelOffset = width * height
                    outputStride = 2
                }
            }
            val buffer = planes[i].buffer
            val rowStride = planes[i].rowStride
            val pixelStride = planes[i].pixelStride
            /*if (VERBOSE) {
                Log.v(TAG, "pixelStride " + pixelStride);
                Log.v(TAG, "rowStride " + rowStride);
                Log.v(TAG, "width " + width);
                Log.v(TAG, "height " + height);
                Log.v(TAG, "buffer size " + buffer.remaining());
            }*/
            val shift = if (i == 0) 0 else 1
            val w = width shr shift
            val h = height shr shift
            buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
            for (row in 0 until h) {
                var length: Int
                if (pixelStride == 1 && outputStride == 1) {
                    length = w
                    buffer[data, channelOffset, length]
                    channelOffset += length
                } else {
                    length = (w - 1) * pixelStride + 1
                    buffer[rowData, 0, length]
                    for (col in 0 until w) {
                        data[channelOffset] = rowData[col * pixelStride]
                        channelOffset += outputStride
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
            }
            //if (VERBOSE) Log.v(TAG, "Finished reading data from plane " + i);
        }
        return data
    }

    const val YUV420P = 0
    const val YUV420SP = 1
    const val NV21 = 2
    private const val TAG = "ImageUtil"

    /***
     * 此方法内注释以640*480为例
     * 未考虑CropRect的
     */
    fun getBytesFromImageAsType(image: Image?, type: Int): ByteArray? {
        try {
            //获取源数据，如果是YUV格式的数据planes.length = 3
            //plane[i]里面的实际数据可能存在byte[].length <= capacity (缓冲区总大小)
            val planes = image!!.planes

            //数据有效宽度，一般的，图片width <= rowStride，这也是导致byte[].length <= capacity的原因
            // 所以我们只取width部分
            val width = image.width
            val height = image.height

            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
            val yuvBytes =
                ByteArray(width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8)
            //目标数组的装填到的位置
            var dstIndex = 0

            //临时存储uv数据的
            val uBytes = ByteArray(width * height / 4)
            val vBytes = ByteArray(width * height / 4)
            var uIndex = 0
            var vIndex = 0
            var pixelsStride: Int
            var rowStride: Int
            for (i in planes.indices) {
                pixelsStride = planes[i].pixelStride
                rowStride = planes[i].rowStride
                val buffer = planes[i].buffer

                //如果pixelsStride==2，一般的Y的buffer长度=640*480，UV的长度=640*480/2-1
                //源数据的索引，y的数据是byte中连续的，u的数据是v向左移以为生成的，两者都是偶数位为有效数据
                val bytes = ByteArray(buffer.capacity())
                buffer[bytes]
                var srcIndex = 0
                if (i == 0) {
                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
                    for (j in 0 until height) {
                        System.arraycopy(bytes, srcIndex, yuvBytes, dstIndex, width)
                        srcIndex += rowStride
                        dstIndex += width
                    }
                } else if (i == 1) {
                    //根据pixelsStride取相应的数据
                    for (j in 0 until height / 2) {
                        for (k in 0 until width / 2) {
                            uBytes[uIndex++] = bytes[srcIndex]
                            srcIndex += pixelsStride
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2
                        }
                    }
                } else if (i == 2) {
                    //根据pixelsStride取相应的数据
                    for (j in 0 until height / 2) {
                        for (k in 0 until width / 2) {
                            vBytes[vIndex++] = bytes[srcIndex]
                            srcIndex += pixelsStride
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2
                        }
                    }
                }
            }
            when (type) {
                YUV420P -> {
                    System.arraycopy(uBytes, 0, yuvBytes, dstIndex, uBytes.size)
                    System.arraycopy(vBytes, 0, yuvBytes, dstIndex + uBytes.size, vBytes.size)
                }
                YUV420SP -> {
                    var i = 0
                    while (i < vBytes.size) {
                        yuvBytes[dstIndex++] = uBytes[i]
                        yuvBytes[dstIndex++] = vBytes[i]
                        i++
                    }
                }
                NV21 -> {
                    var i = 0
                    while (i < vBytes.size) {
                        yuvBytes[dstIndex++] = vBytes[i]
                        yuvBytes[dstIndex++] = uBytes[i]
                        i++
                    }
                }
            }
            return yuvBytes
        } catch (e: Exception) {
            image?.close()
            Log.i(TAG, e.toString())
        }
        return null
    }

    /***
     * YUV420 转化成 RGB
     */
    fun decodeYUV420SP(yuv420sp: ByteArray, width: Int, height: Int): IntArray {
        val frameSize = width * height
        val rgb = IntArray(frameSize)
        var j = 0
        var yp = 0
        while (j < height) {
            var uvp = frameSize + (j shr 1) * width
            var u = 0
            var v = 0
            var i = 0
            while (i < width) {
                var y = (0xff and yuv420sp[yp].toInt()) - 16
                if (y < 0) y = 0
                if (i and 1 == 0) {
                    v = (0xff and yuv420sp[uvp++].toInt()) - 128
                    u = (0xff and yuv420sp[uvp++].toInt()) - 128
                }
                val y1192 = 1192 * y
                var r = y1192 + 1634 * v
                var g = y1192 - 833 * v - 400 * u
                var b = y1192 + 2066 * u
                if (r < 0) r = 0 else if (r > 262143) r = 262143
                if (g < 0) g = 0 else if (g > 262143) g = 262143
                if (b < 0) b = 0 else if (b > 262143) b = 262143
                rgb[yp] = (-0x1000000 or (r shl 6 and 0xff0000)
                        or (g shr 2 and 0xff00) or (b shr 10 and 0xff))
                i++
                yp++
            }
            j++
        }
        return rgb
    }


    fun YuvTansformJpeg(data: ByteArray, width: Int, hight: Int, quality: Int): Bitmap? {
        Log.i(TAG, "Yuv开始转换Jpeg")
        try {
            val image_jpeg = YuvImage(data, ImageFormat.NV21, width, hight, null)
            val stream = ByteArrayOutputStream()
            image_jpeg.compressToJpeg(Rect(0, 0, width, hight), quality, stream)
            val jpeg_bitmap =
                BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.toByteArray().size)
            Log.i(TAG, "Yuv转换Jpeg完成")
            return jpeg_bitmap
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "failed :$e")
        }
        return null
    }


    fun adjustPhotoRotation(bm: Bitmap, orientationDegree: Int): Bitmap? {
        val m = Matrix()
        m.setRotate(orientationDegree.toFloat(), bm.width.toFloat() / 2, bm.height.toFloat() / 2)
        try {
            return Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, m, true)
        } catch (ex: OutOfMemoryError) {
        }
        return null
    }


    fun adjustPhotoRotation2(bm: Bitmap, orientationDegree: Int): Bitmap? {
        val m = Matrix()
        m.setRotate(orientationDegree.toFloat(), bm.width.toFloat() / 2, bm.height.toFloat() / 2)
        val targetX: Float
        val targetY: Float
        if (orientationDegree == 90) {
            targetX = bm.height.toFloat()
            targetY = 0f
        } else {
            targetX = bm.height.toFloat()
            targetY = bm.width.toFloat()
        }
        val values = FloatArray(9)
        m.getValues(values)
        val x1 = values[Matrix.MTRANS_X]
        val y1 = values[Matrix.MTRANS_Y]
        m.postTranslate(targetX - x1, targetY - y1)
        val bm1 = Bitmap.createBitmap(bm.height, bm.width, Bitmap.Config.ARGB_8888)
        val paint = Paint()
        val canvas = Canvas(bm1)
        canvas.drawBitmap(bm, m, paint)
        return bm1
    }


    fun rawByteArray2RGBABitmap2(data: ByteArray, width: Int, height: Int): Bitmap? {
        val frameSize = width * height
        val rgba = IntArray(frameSize)
        for (i in 0 until height) for (j in 0 until width) {
            var y = 0xff and data[i * width + j].toInt()
            val u = 0xff and data[frameSize + (i shr 1) * width + (j and 1.inv()) + 0].toInt()
            val v = 0xff and data[frameSize + (i shr 1) * width + (j and 1.inv()) + 1].toInt()
            y = if (y < 16) 16 else y
            var r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128))
            var g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128))
            var b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128))
            r = if (r < 0) 0 else if (r > 255) 255 else r
            g = if (g < 0) 0 else if (g > 255) 255 else g
            b = if (b < 0) 0 else if (b > 255) 255 else b
            rgba[i * width + j] = -0x1000000 + (b shl 16) + (g shl 8) + r
        }
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bmp.setPixels(rgba, 0, width, 0, 0, width, height)
        return bmp
    }

}