package com.sucheon.alarm.mock;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.AlgData;
import com.sucheon.alarm.event.EventBean;
import com.sucheon.alarm.event.PointData;
import com.sucheon.alarm.mock.notify.EventReceviverByInterrupt;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 特征值数据模拟 (多线程顺序生成)
 */
@Slf4j
public class FeatureDataAutoGen {


    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "192.168.3.48:30092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // 创建多个线程，并行执行
        genBatch(props, "scpc.alarm");
    }


    /**
     * 10个线程分发数据
     * @param props
     * @param topic
     */
    private static void genBatch(Properties props, String topic){

        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(10);



        Map<String, EventReceviverByInterrupt> eventReceviverByInterruptHashMap = new HashMap<>();
        for (int i=0;i<100;i++) {
            EventReceviverByInterrupt eventReceviverByInterrupt = new EventReceviverByInterrupt("worker-" + i);
            eventReceviverByInterruptHashMap.put("worker-"+i, eventReceviverByInterrupt);
        }

        Master master = new Master(countDownLatch, eventReceviverByInterruptHashMap);
        List<Worker> workers  = new ArrayList<>();

        log.info("分发数据准备开始.....");


        for (int i=0;i <100; i++) {
            EventReceviverByInterrupt eventReceviverByInterrupt = eventReceviverByInterruptHashMap.get("worker-" + i);
            Worker worker = new Worker(countDownLatch, eventReceviverByInterrupt,"worker-" + i, new ThreadLocal<List<? extends EventBean>>()) {

                @Override
                public String threadName() {
                    return getName();
                }

                @Override
                public List<? extends EventBean> callbackProcess(ThreadLocal<List<? extends EventBean>> currentThreadLocal) {
                    log.info(String.format("当前线程: %s, 开始分发数据.... ", getName()));


                    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);
                    List<PointData> pointDataList = new ArrayList<>();
                    PointData currentData = new PointData();
                    try {
                        for (int i = 0; i < 100; i++) {
                            PointData pointTree = assemblePointData();
                            AlgData algData = assembleAlgData();
                            String valueStr = i % 2==0 ? CommonConstant.objectMapper.writeValueAsString(pointTree):CommonConstant.objectMapper.writeValueAsString(algData);
                            currentData = pointTree;
                            ProducerRecord<String, String> record = new ProducerRecord<>(topic, valueStr);
                            kafkaProducer.send(record);
                            pointDataList.add(pointTree);
                        }
                        currentThreadLocal.set(pointDataList);
                        log.info(String.format("当前线程: %s, 本批次: %s 数据发送完成 ", getName(), getBatchByThreadName()));

                    }catch (Exception ex) {
                        System.out.printf("哪一条数据出了问题: %s%n", JSON.toJSONString(currentData));
                        return pointDataList;
                    }
                    return pointDataList;
                }

            };
            workers.add(worker);
        }
        executorService.execute(master);
        for (int i =0; i<10;i++){
            Worker worker = workers.get(i);
            executorService.execute(worker);
        }

        executorService.shutdown();
    }

    /**
     * 可能会有多个测点
     * @return
     */
    public static PointData assemblePointData() {
        PointData pointData = new PointData();
        // 创建Random对象
        Random random = new Random();
        String characters = "0123456789";
        // 创建一个StringBuilder对象用于存储生成的验证码
        StringBuilder codeBuilder = new StringBuilder();
        // 生成四位验证码
        for (int i = 0; i < 4; i++) {
            // 生成一个随机的索引，范围是0到characters的长度-1
            int index = random.nextInt(characters.length());
            // 根据索引从characters中获取对应的字符，并追加到codeBuilder中
            codeBuilder.append(characters.charAt(index));
        }

        UUID uuid = UUID.randomUUID();

        //设置毫秒
        LocalDateTime now = LocalDateTime.now();
        long time8 = now.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        pointData.setDeviceTimeStamp(time8);
        pointData.setPageNum(8);

        pointData.setPointId(codeBuilder.toString());
        pointData.setDeviceChannel(uuid.toString());
        pointData.setBatchId("E45F019D4DA9_123456789_001");

        pointData.setBandSpectrum("19.15,18.87,19.28,17.6,17.55,17.57,17.23,16.92,16.59,16.22,15.9,15.54,15.11,14.77,14.55,14.36,13.97,13.43,12.92,12.26");
        pointData.setFeature1("25.39,25.52,25.52,25.53,25.49,25.57,25.55,25.55,25.54,25.52,25.53,25.5,25.54,25.53,25.48,25.52,25.46,25.5,25.47,25.52,25.51,25.51,25.52,25.51");
        pointData.setFeature2("20.92,21.28,21.11,21.11,21.32,21.01,20.84,21.17,21.17,21.09,21.1,21.15,21.22,21.12,21.01,21.18,21.23,21.16,21.27,21.1,21.1,21.01,21.12,21.24");
        pointData.setFeature3("17.21,17.4,17.38,17.38,17.36,17.42,17.37,17.43,17.4,17.36,17.38,17.32,17.41,17.35,17.32,17.39,17.34,17.37,17.35,17.38,17.36,17.35,17.38,17.38");
        pointData.setFeature4("6.97,6.92,6.9,6.91,6.93,6.93,6.96,6.89,6.96,6.98,6.88,6.88,6.96,6.88,6.87,6.93,6.87,6.94,6.86,6.93,6.9,6.88,6.9,6.91");
        pointData.setPeakPowers("15.22,15.15,15.2,14.59,14.94,14.94,14.59,14.47,14.24,14.31,14.37,14.35,14.2,14.2,14.15,14.2,14.16,14.47,14.43,14.34");
        pointData.setPeakFreqs("4.25,5.29,5.72,5.93,6.12,6.35,6.49,6.65,6.79,6.88,6.97,7.04,7.13,7.21,7.26,7.31,7.38,7.44,7.5,7.55");
        pointData.setMean(34850480);
        pointData.setCustomFeature("681661.94,736165.2,716029.3,618464.56,677835.06,837895.4,960856.6,876057.25,830634.56,885900.5,1066238.8,1078987.0,829344.44,839599.3,1048741.5,921391.8,963481.9,953602.75,908339.4,809589.3,839283.25,716865.2,654791.3,975769.44,991018.06,1066329.9,864750.5,813280.4,1036468.44,888527.06,830588.25,1020584.6,1014301.6,699182.06,573560.9,786797.94,940245.25,941647.25,690120.94,768141.44,840565.8,963289.0,950596.94,963170.9,903096.44,718430.75,690843.9,760362.44,942086.56,953639.4,750692.1,861516.44,858321.56,870012.06,858920.3,879094.94,869404.4,953772.94,740978.3,792139.9,922848.9,780031.06,845162.56,977363.0,1030268.8,833865.75,894567.94,761946.7,866780.2,987864.6,772584.0,820971.9,887418.9,706747.44,706761.25,843298.7,950446.44,945241.7,817593.25,749271.06,818605.0,897157.5,817803.2,875729.94,900032.0,842389.94,811751.56,830925.9,978231.4,967871.44,769122.56,648977.6,691984.56,838817.94,1020926.8,849678.2,1001613.75,968575.75,635898.1,651513.8,790355.9,913735.06,763031.25,843809.0,887035.8,792661.4,925545.75,890163.75,991395.75,1196972.0,1050239.1,962178.44,1053948.1,1054552.6,753442.25,774831.75,891516.75,680114.0,954449.94,855216.4,588692.56,678221.6,767777.3,682007.06,695613.7,883726.0,802134.75,836528.06,984354.44,953268.6,862764.2,858541.44,807043.8,902995.7,1112549.5,1012588.75,831205.0,1007917.75,827403.7,810097.3,785137.5,855310.75,1080306.0,886907.3,835372.3,817304.44,770249.8,873330.6,622133.06,790377.1,865815.0,909392.75,946515.6,1018217.1,1117330.8,1017854.4,892765.44,699523.4,737667.94,695222.9,822683.4,768623.0,616880.0,691929.5,838693.7,971639.6,869916.5,953783.4,851180.25,929063.44,759712.7,709418.75,693171.1,711011.4,796858.8,803888.3,699270.44,811997.3,788185.94,825782.8,992187.44,865179.25,753936.5,653191.25,833651.2,1062621.1,760469.8,958452.4,1097027.2,964256.8,787677.0,824895.44,876586.6,858976.94,851966.06,917716.06,757808.56,742264.1,852068.6,875565.0,925659.2,974016.1,749185.2,793456.1,930003.6,1031860.25,840159.56,778311.94,643588.25,503059.72,642966.44,806416.25,1073819.8,839917.94,776387.1,618182.06,645196.0,784914.1,728568.3,749316.0,1038543.7,870313.5,871618.06,823484.1,727182.44,766922.56,738774.4,710621.75,787943.44,902549.9,1028903.44,990488.3,902969.1,940031.2,763441.75,785915.1,916344.1,1075539.9,983691.2,1080013.5,956954.7,914863.25,898173.7,747262.94,721386.1,951942.0,1025876.56,1035045.75,834647.25,783270.4,823297.0,797081.0,852782.8,950418.6,866230.06,723180.2,739647.3,931388.1,934856.9,871786.44,907132.06,808768.5,885641.0,776926.56,762788.06,698022.0,624757.4,735563.2,972882.7,915411.9,986076.94,832920.0,854556.4,927574.06,917762.1,760766.7,1027057.25,933505.8,785986.75,1000737.0,1036433.0,820184.5,972555.9,887691.25,908078.0,700475.5,845392.1,854935.75,957642.25,772032.9,833977.75,931038.0,936035.1,915557.3,896698.56,880100.0,829150.25,760054.2,623023.6,677446.5,771959.6,793553.25,871837.25,744007.7,885993.75,1041044.1,1014428.4,1054466.9,1063196.0,820200.1,1024951.44,1127090.4,989493.0,829359.1,874584.3,985352.6,874156.75,891871.9,750376.9,906331.7,842531.3,801532.75,1025109.94,883854.7,865887.2,925060.1,742299.56,830263.4,935813.4,950237.5,917530.0,858505.2,801779.6,727552.25,758868.8,781462.56,665695.2,712865.5,894781.25,1000841.75,916235.44,899722.4,750643.56,679894.8,773067.8,977730.2,796379.2,878035.56,978356.5,902744.06,948681.5,841914.75,939339.5,761706.94,954701.75,1118617.8,789844.9,695655.9,778047.4,941290.5,1017536.3,627246.25,734661.25,825231.3,865811.94,998668.94,1095980.5,977662.4,820212.1,678840.25,670922.94,775760.0,955024.94,1017712.8,1174317.8,1123276.9");
        pointData.setStd(1008);
        pointData.setMeanLf(1505013249);
        pointData.setSpeed("0.8,0.11");
        pointData.setOriginalVibrate("5.22480558 5.22480558e,5.22480558");
        pointData.setVersion(0);
        pointData.setTemperature(-274);
        pointData.setTime(1654247225948L);
        pointData.setMeanHf(120179522302L);
        return pointData;
    }

    /**
     * 组装算法实例数据
     * @return
     */
    public static AlgData assembleAlgData(){
        UUID uuid = UUID.randomUUID();
        AlgData algData = new AlgData();
        algData.setBatchId("E45F019D4DA9_123456789_001");
        algData.setCode("tcvd_vsfdd_frr3r3_cdd");
        algData.setDeviceChannel(uuid.toString());
        //设置毫秒
        LocalDateTime now = LocalDateTime.now();
        long time8 = now.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        algData.setDeviceTimestamp(time8);
        algData.setCode(String.valueOf(8434343));
        algData.setGroupKey("field1,field2,field3,field4");

        // 创建Random对象
        Random random = new Random();
        String characters = "0123456789";
        // 创建一个StringBuilder对象用于存储生成的验证码
        StringBuilder codeBuilder = new StringBuilder();
        return algData;

    }
}
