package com.ivanzhao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName Application
 * @Description 启动入口
 * @Author IvanZhao
 * @Date 2026/9/22 11:19
 *       Version 1.0
 */
/**
 * @SpringBootApplication: Spring Boot 核心复合注解
 *                         1. @SpringBootConfiguration: 标识当前类为配置类
 *                         2. @EnableAutoConfiguration: 开启 Spring Boot 自动装配（自动加载
 *                         Tomcat、MyBatis 等默认配置）
 *                         3. @ComponentScan:
 *                         开启组件扫描，默认自动扫描当前包（com.ivanzhao）及其所有子包下的 @Service、@Component、@RestController
 *                         等 Bean
 */
@SpringBootApplication

/**
 * @Configurable: 允许 Spring 对非 Spring 管理的对象（如 new 出来的对象）进行依赖注入
 */
@Configurable

/**
 * @EnableScheduling: 开启 Spring 定时任务调度功能
 *                    必须添加此注解，Spring 才会启动后台线程池去执行标注了 @Scheduled 的 Job
 *                    类（如掉单补偿、超时关单）
 */
@EnableScheduling

/**
 * @MapperScan: 指定 MyBatis Mapper 接口的扫描路径
 *              扫描 com.ivanzhao.dao 包下的所有数据访问接口，自动动态生成代理实现类并注入到 Spring 容器中
 */
@MapperScan("com.ivanzhao.dao")
public class Application {

    /**
     * Spring Boot 应用程序主入口方法
     * 
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        // 启动 Spring 容器与内置嵌入式 Tomcat Web 服务器
        SpringApplication.run(Application.class, args);
    }

}
