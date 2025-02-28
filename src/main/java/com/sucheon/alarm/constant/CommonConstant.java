package com.sucheon.alarm.constant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sucheon.alarm.utils.JsonConfigUtils;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;


public class CommonConstant {

    public static final ObjectMapper objectMapper = JsonConfigUtils.getObjectMapper();

    public static final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();

    public static final String alarmTopic = "scpc.alarm";

    /**
     * 解析出来的缓存key
     */
    public static final String cacheKey = "parserCacheKey";

    /**
     * cep报警发生步骤定义
     */
    public static final String cepAlertStep = "alert";

    /**
     * cep报警恢复步骤定义
     */
    public static final String cepRecoverStep = "recover";

}
