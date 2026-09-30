package com.ivanzhao.controller.job;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ivanzhao.service.IOrderService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 超时未支付关单定时任务
 * 定时清理超过 30 分钟未付款的废弃订单
 */
@Slf4j
@Component
public class TimeoutCloseOrderJob {
    @Resource
    private IOrderService orderService;

    /**
     * 定时执行超时未支付关单任务
     * cron = "0 0/10 * * * ?" 表示：每隔 10 分钟自动触发执行一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            List<String> orderIds = orderService.queryTimeOutCloseOrderList();
            if (null == orderIds || orderIds.isEmpty()) {
                return;
            }

            for (String orderId : orderIds) {
                boolean success = orderService.changeOrderClose(orderId);
                log.info("超时订单关单,订单号:{}，关单结果：{}", orderId, success);
            }
        } catch (Exception e) {
            log.error("超时订单关单失败", e);
        }
    }

}
