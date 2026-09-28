package com.ivanzhao.config;

import com.ivanzhao.service.weixin.IWeixinApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @ClassName Retrofit2Config
 * @Description Retrofit2配置类,实例微信的api接口
 * @Author IvanZhao
 * @Date 2026/9/27 11:07
 *       Version 1.0
 */
@Slf4j
@Configuration
public class Retrofit2Config {

    private static final String BASE_URL = "https://api.weixin.qq.com/";

    /**
     * Retrofit2配置
     * @return
     */
    @Bean
    public Retrofit retrofit() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create()) // 自动将 JSON 转为 JavaBean
                .build();
    }

    /**
     * 微信Api服务
     * @param retrofit
     * @return
     */
    @Bean
    public IWeixinApiService wenxinApiService(Retrofit retrofit) {
        return retrofit.create(IWeixinApiService.class);
    }
}
