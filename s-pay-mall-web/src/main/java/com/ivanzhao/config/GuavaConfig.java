package com.ivanzhao.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
     * Guava EventBus 事件总线
     *
     * @return EventBus 实例
     */
    @Bean
    public com.google.common.eventbus.EventBus eventBus() {
        return new com.google.common.eventbus.EventBus();
    }

}
