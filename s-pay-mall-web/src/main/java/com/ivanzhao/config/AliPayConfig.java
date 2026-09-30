package com.ivanzhao.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description 支付宝客户端配置类，负责创建并向 Spring 容器注入 AlipayClient 单例 Bean
 * @Author IvanZhao
 * @Date 2026/9/29
 *       Version 1.0
 */
@Configuration
// 显式启用并注册指定的属性配置类（Bean）到 Spring 容器中
@EnableConfigurationProperties(AliPayConfigProperties.class)
public class AliPayConfig {

    @Bean("alipayClient")
    public AlipayClient alipayClient(AliPayConfigProperties properties) {
        // 2. 自动把绑定好属性的 properties 传进来
        // 3. 用 properties 里的配置参数去初始化 AlipayClient
        return new DefaultAlipayClient(
                properties.getGatewayUrl(),
                properties.getApp_id(),
                properties.getMerchant_private_key(),
                properties.getFormat(),
                properties.getCharset(),
                properties.getAlipay_public_key(),
                properties.getSign_type());
    }

}
