package com.hankcs.xyy.utils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

public class HttpClientUtils {

    public static final RestTemplate restTemplate;

    static {
        // 设置最大连接数、每个Host的默认最大连接数、每个连接的最大存活时长
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(50)
                .setConnectionTimeToLive(10, TimeUnit.SECONDS);
        // 设置是否开启线程池超时连接的探针器。
        httpClientBuilder.evictIdleConnections(5L, TimeUnit.SECONDS);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(3000);
        httpComponentsClientHttpRequestFactory.setConnectTimeout(3000);
        httpComponentsClientHttpRequestFactory.setReadTimeout(3000);
        restTemplate = new RestTemplate(httpComponentsClientHttpRequestFactory);
    }
}
