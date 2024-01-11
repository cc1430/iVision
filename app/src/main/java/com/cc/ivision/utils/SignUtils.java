package com.cc.ivision.utils;

import android.text.TextUtils;
import android.util.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUtils {

    public static String encodeSign(String app_key, String secret) {
        String tempSign = "";
        try {
            String tempStr = secret + "app_key"+app_key + secret;

            tempSign = md5(tempStr);

        } catch (Exception e) {
            tempSign = "";
            e.printStackTrace();
        } finally {
            return tempSign;
        }


    }

    /**
     * 字符串MD5加密
     * @param content
     * @return
     */
    public static String md5(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException",e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * sign 签名 （参数名按ASCII码从小到大排序（字典序）+key+MD5+转大写签名）
     * @param map
     * @return
     */
   /* public static String encodeSign(SortedMap<String,String> map, String key){
        if(StringUtils.isEmpty(key)){
            throw new RuntimeException("签名key不能为空");
        }
        Set<Map.Entry<String, String>> entries = map.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
        List<String> values = new ArrayList<>();

        values.add(key);
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String k = String.valueOf(entry.getKey());
            String v = String.valueOf(entry.getValue());
            if (StringUtils.isNotEmpty(v) && entry.getValue() !=null && !"sign".equals(k) && !"key".equals(k)) {
                values.add(k + "" + v);
            }
        }
        values.add(key);
        String sign = StringUtils.join(values, "");
        return encodeByMD5(sign).toUpperCase();
    }
    *//**
     * 通过MD5加密
     *
     * @param algorithmStr
     * @return String
     *//*
    public static String encodeByMD5(String algorithmStr) {
        if (algorithmStr==null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM_MD5);
            messageDigest.update(algorithmStr.getBytes("utf-8"));
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }*/

    /**
     * 将图片转换成Base64编码的字符串
     */
    public static String fileToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }


}
