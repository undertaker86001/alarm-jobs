package com.sucheon.alarm.utils;


import com.sucheon.alarm.exception.DistrbuteIOException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * java8中实现发送http的异步请求
 */
public class HttpAsyncClientUtil {

    /**
     * 同步获取count个请求的结果
     * @param url
     * @param header
     * @param param
     * @param count
     * @return
     */
    public static String[] getSyncHttpResult(String url,Map<String,String> param,Map<String,String> header,Integer count) throws DistrbuteIOException {
        String[] results = new String[count];
        // 循环获取请求结果
        for (Integer i = 0; i < count; i++) {
            results[i] = HttpUrlConnectionUtil.doGet(url, param, header);
        }
        return results;
    }

    /**
     * 异步获取count个请求的结果,通过java8原生的多线程异步方式来实现
     * @param url
     * @param header
     * @param param
     * @param count
     * @return
     */
    public static String[] getAsyncHttpResult(String url,Map<String,String> param,Map<String,String> header,Integer count){
        CompletableFuture<String>[] futures = new CompletableFuture[count];
        // 循环获取请求结果
        for (Integer i = 0; i < count; i++) {
            futures[i] = asyncSentHttp(url,param,header);
        }
        String[] results = new String[count];
        // 等待异步任务完成，超时时间为5秒
        try {
            CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("请求超时");
            e.printStackTrace();
            return results;
        }
        // 输出每个异步任务的结果
        for (int i = 0; i < count; i++) {
            try {
//                System.out.println(futures[i].get());
                results[i] = futures[i].get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return results;
    }


    /**
     * 异步分页获取count个请求的结果,并且将每页的结果进行提前处理
     * 通过java8原生的多线程异步方式来实现
     * @param url
     * @param param
     * @param header
     * @param totalCount  总请求数
     * @param perPageCount  每页处理请求数
     * @return
     */
    public static String[] getAsyncHttpResultByPage(String url,Map<String,String> param,Map<String,String> header,Integer totalCount,Integer perPageCount){
        // 按照每页大小划分次数
        int handleCount = totalCount % perPageCount == 0 ? (totalCount / perPageCount) : (totalCount / perPageCount + 1);
        List<CompletableFuture<String>> futures = new ArrayList<>();
        // 最终的结果集
        List<String> resultList = new ArrayList<>();
        // 每页初始得到的结果数
        AtomicInteger initCount = new AtomicInteger();
        // 循环获取请求结果
        for (Integer i = 0; i < totalCount; i++) {
            CompletableFuture<String> singleResult = asyncSentHttp(url, param, header);
            futures.add(singleResult);
            initCount.getAndIncrement();
            if (initCount.get() == handleCount){
                handlePageResult(futures,resultList);
                // 完成一页循环后就将初始指针置为0
                initCount.set(0);
                // 将结果集也给置空
                futures.clear();
            }
        }
        if (!futures.isEmpty()){
            handlePageResult(futures,resultList);
        }
        return resultList.toArray(new String[resultList.size()]);
    }

    /**
     * 处理每页的结果
     * @param futures
     * @param resultList
     */
    private static void handlePageResult(List<CompletableFuture<String>> futures,List<String> resultList) {
        // 等待异步任务完成，超时时间为5秒
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[1])).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("请求超时");
            e.printStackTrace();
        }
        // 输出每个异步任务的结果
        for (int i = 0; i < futures.size(); i++) {
            try {
                System.out.println(futures.get(i).get());
                resultList.add(futures.get(i).get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 异步发送http请求
     * @param url
     * @param param
     * @param header
     * @return
     */
    private static CompletableFuture<String> asyncSentHttp(String url, Map<String, String> param, Map<String, String> header) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return HttpUrlConnectionUtil.doGet(url, param, header);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }


    public static void main(String[] args) {
        Map<String, String> param = new LinkedHashMap<>();
        param.put("page","1");
        param.put("pageSize","1");
        Map<String, String> header =  new LinkedHashMap<>();
        header.put("CANPOINTTOKEN","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyR3VpZCI6IlIyWlBNMjE0VUVnNVJHTm5aMEZ1UlRkUGJsRTVVVDA5IiwiZXhwIjoxNjg1NTk3MTcyfQ.LUh1XiGqV0enlQYddjempyrOq3tvAOEm1PbGDSeUDFI");
        header.put("Content-Type","application/Json; charset=UTF-8");
        // 同步发送请求
//        String[] syncResult = getSyncHttpResult("http://39.105.162.131:8080/itembank/ques/quesList", param, header, 3);
//        System.out.println(syncResult);
        // 异步发送请求
//        String[] asyncResult = getAsyncHttpResult("http://39.105.162.131:8080/itembank/ques/quesList", param, header, 3);
//        System.out.println(Arrays.toString(asyncResult));
        // 异步分页发送处理请求
        String[] asyncResultPage = getAsyncHttpResultByPage("http://39.105.162.131:8080/itembank/ques/quesList", param, header, 10,3);
        System.out.println(Arrays.toString(asyncResultPage));
    }



}
