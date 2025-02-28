package com.sucheon.alarm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.DataPoint;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 计算历史报警最高峰值
 */
@Slf4j
public class ComputeUtils {

    /**
     * 假设有无数根柱子，每一根相当于此时的报警等级峰值
     * @param height
     * @return
     */
    public static int computeHistoryAlarmValue(int[] height){
        if (height == null || height.length ==0) { return 0; }

        int n = height.length;
        int[] leftMax = new int[n]; //左边部分
        int[] rightMax = new int[n]; //右边部分
        leftMax[0] = height[0]; //初始化
        rightMax[n - 1] = height[n - 1];
        for (int i=1; i<n; i++){
            leftMax[i] = Math.max(leftMax[i-1], height[i]); //更新最大值
        }

        for (int i=n-2;i>=0;i--){
            rightMax[i] = Math.max(rightMax[i+1], height[i]);
        }
        int ans = 0;
        for (int i=0;i<n;i++){
            ans += Math.min(leftMax[i], rightMax[i]) - height[i];
        }

        return ans;
    }


    public static DataPoint[] findPeaks(DataPoint[] nums) {
        if (nums == null || nums.length < 2) {
            return new DataPoint[0]; // 空数组或只有一个元素，返回空数组
        }

        int firstMax = Integer.MIN_VALUE;
        int secondMax = Integer.MAX_VALUE;
        long firstTimestamp = 0;
        long secondTimestamp = 0;

        for (DataPoint num : nums) {
            if (num.getValue() > firstMax) {
                secondMax = firstMax;
                firstMax = num.getValue();
                firstTimestamp = num.getTimestamp();
            } else if (num.getValue() > secondMax && num.getValue() != firstMax) {
                secondMax = num.getValue();
                secondTimestamp = num.getTimestamp();
            }
        }

        return new DataPoint[]{ new DataPoint(firstMax, firstTimestamp), new DataPoint(secondMax, secondTimestamp) };
    }

    private static long convertToTimestamp(String timestampStr) {
        Date date = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = sdf.parse(timestampStr);
        } catch (Exception ex){
            String errorMessage = ExceptionUtil.getErrorMessage(ex);
            log.error("当前错误消息: {}", errorMessage);
        }
        return date.getTime();
    }



    public static void main(String[] args) throws JsonProcessingException {


        int[] height = new int[]{0,1,0,2,1,0,1,3,2,1,2,1};
        ComputeUtils computeUtils = new ComputeUtils();
        int ans = computeUtils.computeHistoryAlarmValue(height);
        System.out.println(ans);


        DataPoint[] data = { new DataPoint(1, convertToTimestamp("2021-04-01 11:20:11")),
                             new DataPoint(-3, convertToTimestamp("2021-04-01 11:30:01")),
                             new DataPoint(4, convertToTimestamp("2021-04-01 11:20:29")),
                            new DataPoint(1, convertToTimestamp("2021-04-01 11:40:43"))};

        DataPoint[] records = findPeaks(data);
        System.out.println(CommonConstant.objectMapper.writeValueAsString(records));

    }
}
