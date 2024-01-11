package com.cc.ivision.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtils {
    private static final String TAG = "FileUtils";


    private static String getTxtPath(Context context) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        final File dir = new File(externalStorageDirectory, "AiGesture");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return  (dir.getAbsolutePath() + "/aigesture_") + System.currentTimeMillis() + ".txt";
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(Context context, String content) {
        //生成文件夹之后，再生成文件，不然会出错
        //makeFile(filePath, fileName);
        String mFilePath = getTxtPath(context);
        // 每次写入时，都换行写
        String mContent = content + "rnnn";
        try {
            File file = new File(mFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile mRandomAccessFile = new RandomAccessFile(file, "rwd");
            mRandomAccessFile.seek(file.length());
            mRandomAccessFile.write(mContent.getBytes());
            mRandomAccessFile.close();
        } catch (IOException e) {
        }
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String content, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFile(filePath, fileName);
        String mFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String mContent = content + "rnnn";
        try {
            File file = new File(mFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile mRandomAccessFile = new RandomAccessFile(file, "rwd");
            mRandomAccessFile.seek(file.length());
            mRandomAccessFile.write(mContent.getBytes());
            mRandomAccessFile.close();
        } catch (IOException e) {
        }
    }
    //生成文件
    public static File makeFile(String filePath, String fileName) {
        File file = null;
        makeDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
        }
        return file;
    }
    //生成文件夹
    public static void makeDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
        }
    }
}
