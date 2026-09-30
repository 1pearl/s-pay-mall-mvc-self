package com.ivanzhao.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.ivanzhao.controller.listener.OrderPaySuccessListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName GuavaConfig
 * @Description Guava缓存配置类
 * @Author IvanZhao
 * @Date 2026/9/27 10:35
 *       Version 1.0
 */
@Configuration
public class GuavaConfig {

    /**
     * 微信AccessToken缓存
     * 
     * @return
     */
    @Bean(name = "weixinAccessToken")
    public Cache<String, String> weixinAccessToken() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .build();
    }

    /**
     * OpenidToken缓存
     * 
     * @return
     */
    @Bean(name = "openidToken")
    public Cache<String, String> openidToken() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    /**
     * Guava EventBus 事件总线（并自动注册支付成功监听器）
     *
     * @return EventBus 实例
     */
    @Bean(name = "eventBus")
    public EventBus eventBus(OrderPaySuccessListener orderPaySuccessListener) {
        EventBus eventBus = new EventBus();
        eventBus.register(orderPaySuccessListener);
        return eventBus;
    }

}
