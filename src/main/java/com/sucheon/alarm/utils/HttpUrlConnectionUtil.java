package com.sucheon.alarm.utils;


import com.sucheon.alarm.config.UserProperties;
import com.sucheon.alarm.exception.DistrbuteIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Component
public class HttpUrlConnectionUtil {

    @Autowired
    private UserProperties userProperties;


    /**
     * Http get请求
     * @param httpUrl 连接
     * @return 响应数据
     */
    public static String doGet(String httpUrl, Map<String,String> param, Map<String,String> header) throws DistrbuteIOException {
        //链接
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuffer result = new StringBuffer();
        try {
            // 设置请求参数
            StringBuilder builder = new StringBuilder();
            if (param != null && param.size() != 0){
                builder.append("?");
                for (Map.Entry<String, String> entry : param.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    builder.append(key+"="+value).append("&");
                }
            }
            httpUrl += builder.toString();
            //创建连接
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //设置连接超时时间
            connection.setConnectTimeout(15000);
            //设置读取超时时间
            connection.setReadTimeout(15000);
            // 设置请求头参数
            if (header != null && header.size() != 0){
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    connection.setRequestProperty(key,value);
                }
            }
            //开始连接
            connection.connect();
            //获取响应数据
            if (connection.getResponseCode() == 200) {
                //获取返回的数据
                is = connection.getInputStream();
                if (null != is) {
                    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String temp = null;
                    while (null != (temp = br.readLine())) {
                        result.append(temp);
                    }
                }
            }
        } catch (IOException e) {
            throw new DistrbuteIOException("本次Http连接失败, 原因为:  ", e.getCause());
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new DistrbuteIOException("本次Http连接失败, 原因为:  ", e.getCause());
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new DistrbuteIOException("本次Http连接失败, 原因为:  ", e.getCause());
                }
            }
            //关闭远程连接
            connection.disconnect();
        }
        return result.toString();
    }

    /**
     * Http post请求
     * @param httpUrl 连接
     * @param param 参数
     * @return
     */
    public static String doPost(String httpUrl, @Nullable String param, Map<String,String> header) throws DistrbuteIOException {
        StringBuffer result = new StringBuffer();
        //连接
        HttpURLConnection connection = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            //创建连接对象
            URL url = new URL(httpUrl);
            //创建连接
            connection = (HttpURLConnection) url.openConnection();
            //设置请求方法
            connection.setRequestMethod("POST");
            //设置连接超时时间
            connection.setConnectTimeout(15000);
            //设置读取超时时间
            connection.setReadTimeout(15000);
            //DoOutput设置是否向httpUrlConnection输出，DoInput设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            //设置是否可读取
            connection.setDoOutput(true);
            connection.setDoInput(true);
            //设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            // 设置自定义请求头参数
            if (header != null && header.size() != 0){
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    connection.setRequestProperty(key,value);
                }
            }
            //拼装参数
            if (null != param && param.equals("")) {
                //设置参数
                os = connection.getOutputStream();
                //拼装参数
                os.write(param.getBytes("UTF-8"));
            }
            //设置权限
            //设置请求头等
            //开启连接
            //connection.connect();
            //读取响应
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                if (null != is) {
                    br = new BufferedReader(new InputStreamReader(is, "GBK"));
                    String temp = null;
                    while (null != (temp = br.readLine())) {
                        result.append(temp);
                        result.append("\r\n");
                    }
                }
            }

        } catch (IOException e) {
            throw new DistrbuteIOException("本次Http连接失败, 原因为:  ", e.getCause());
        }  finally {
            //关闭连接
            if(br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    throw new DistrbuteIOException("本次Http连接失败, 原因为:  ", e.getCause());
                }
            }
            if(os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    throw new DistrbuteIOException("本次Http连接失败, 原因为:  ", e.getCause());
                }
            }
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    throw new DistrbuteIOException("本次Http连接失败, 原因为:  ", e.getCause());
                }
            }
            //关闭连接
            connection.disconnect();
        }
        return result.toString();
    }



}
