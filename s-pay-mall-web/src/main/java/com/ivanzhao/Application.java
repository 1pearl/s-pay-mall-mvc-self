package com.ivanzhao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName Application
 * @Description 启动入口
 * @Author IvanZhao
 * @Date 2026/9/22 11:19
 * Version 1.0
 */
@SpringBootApplication
@Configurable
@MapperScan("com.ivanzhao.dao")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
