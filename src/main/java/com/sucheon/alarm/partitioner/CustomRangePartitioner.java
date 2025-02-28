package com.sucheon.alarm.partitioner;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.AlarmMatchResult;
import com.sucheon.alarm.exception.DistrbuteException;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;
import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 根据不同并行度并行写入多个分区
 * 原生的FlinkFixPartitoner会存在导致有的partition无法写到
 * 参考链接: https://blog.csdn.net/weixin_30725467/article/details/95010242
 *
 */
@Slf4j
public class CustomRangePartitioner<T> extends FlinkKafkaPartitioner<T>
        implements Serializable {

    private final ConcurrentHashMap<String, AtomicInteger> topicCountMap = new ConcurrentHashMap<>();
    private Integer parallelInstanceId;
    private Integer parallelInstances;

    public CustomRangePartitioner() {

    }

    @Override
    public void open(int parallelInstanceId, int parallelInstances) {

        Preconditions.checkArgument(parallelInstanceId >=0, "Id of subTask cannot be negative");
        Preconditions.checkArgument(parallelInstances > 0, "Number of subtasks must be large than 0");
        this.parallelInstanceId = parallelInstanceId;
        this.parallelInstances = parallelInstances;
    }

    @Override
    public int partition(
            T next,
            byte[] serializedKey,
            byte[] serializedValue,
            String targetTopic,
            int[] partitions) {


        try {
            int[] targetPartitions = computePartitions(partitions);
            int length = targetPartitions.length;
            if (length == 1){
                return targetPartitions[0];
            }else {
                int count = nextValue(targetTopic);
                return targetPartitions[length % count];
            }
        } catch (JsonProcessingException e) {
            throw new DistrbuteException("分区分配失败: 原因:", e);
        }
    }

    /**
     * 根据不同topic记录此时进入分发系统的事件数量
     * @param topic
     * @return
     */
    private int nextValue(String topic){
        AtomicInteger counter = this.topicCountMap.computeIfAbsent(topic ,(k) -> { return new AtomicInteger(0);});
        return counter.getAndIncrement();
    }

    /**
     * 使用 分区数 + （循环次数*并行数）就可以轮训分配！当结果大于分区数时候就不再分配
     * @param partitions
     * @return
     * @throws JsonProcessingException
     */
    protected int[] computePartitions(int[] partitions) throws JsonProcessingException {

        //如果当前分区数等于并行的subtask数
        if (partitions.length == parallelInstances){
            return new int[]{partitions[parallelInstanceId % partitions.length]};
        }else if (partitions.length > parallelInstances){
            //并行度小于分区数
            int m = (int)Math.ceil((float) partitions.length / parallelInstances);
            List<Integer>  parallelPartitionList = new ArrayList<>();
            for (int i=0;i<m;i++){
                int partitionIndex = parallelInstanceId + (i * parallelInstances);
                if ((partitionIndex + 1) <= partitions.length){
                    parallelPartitionList.add(partitions[partitionIndex]);
                }
            }
            log.info(String.format("当前子任务: [%s m=%s] arrs [%s]", parallelInstanceId, m, CommonConstant.objectMapper.writeValueAsString(parallelPartitionList)));

            int[] parallelPartitionArr = new int[parallelPartitionList.size()];

            for (int i=0;i<parallelPartitionList.size(); i++){
                parallelPartitionArr[i] = parallelPartitionList.get(i);
            }
            return parallelPartitionArr;

        }
        else {
            //并行度大于分区数
            return new int[]{partitions[parallelInstanceId % partitions.length]};
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        int[] partitions = new int[18];
        for (int i = 0; i < 18; i++) {
            partitions[i] = i;
        }

        int maxPaller = 9;
        for (int i = 0; i < maxPaller; i++) {
            CustomRangePartitioner partitioner = new CustomRangePartitioner();
            partitioner.open(i, maxPaller);
            partitioner.computePartitions(partitions);
        }
    }

}
