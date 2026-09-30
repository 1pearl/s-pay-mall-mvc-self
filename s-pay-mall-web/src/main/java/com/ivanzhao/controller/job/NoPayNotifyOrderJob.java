package com.ivanzhao.controller.job;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;

import com.ivanzhao.common.contants.Constants;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ivanzhao.service.IOrderService;

import java.util.List;

/**
 * 掉单补偿定时任务
 * 定时检测未接收到或未正确处理的支付回调通知，主动向支付宝发起对账查询
 */
@Slf4j
@Component
public class NoPayNotifyOrderJob {

    /** 订单业务服务（用于查询待补偿订单列表、更新支付成功状态） */
    @Resource
    private IOrderService orderService;

    /** 支付宝客户端（用于向支付宝官方网关发起主动对账查询） */
    @Resource
    private AlipayClient alipayClient;

    /**
     * 定时执行掉单检测与对账补偿任务
     * cron = "0/3 * * * * ?" 表示：每隔 3 秒自动触发执行一次
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void excuteJob() {
        try {
            // 1. 【捞取疑似掉单数据】从数据库中查询下单超过 1 分钟、但状态依然卡在 PAY_WAIT 的订单列表
            List<String> orderIds = orderService.queryNoPayNotifyOrder();
            if (null == orderIds || orderIds.isEmpty()) {
                // 如果没有符合条件的异常订单，直接结束本次任务轮询
                return;
            }

            // 2. 【逐个向支付宝主动查账】遍历这批掉单，主动调用支付宝“统一交易查询接口”
            for (String orderId : orderIds) {
                // 2.1 创建支付宝交易查询请求对象
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

                // 2.2 组装业务查询模型：传入我们商户系统的业务订单号 (out_trade_no)
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(orderId);
                request.setBizModel(bizModel);

                // 2.3 向支付宝沙箱网关发出 HTTP 对账请求，获取交易查询结果
                AlipayTradeQueryResponse response = alipayClient.execute(request);

                // 2.4 获取支付宝返回的网关接口响应码（"10000" 代表接口调用成功且存在交易记录）
                String code = response.getCode();

                // 3. 【判定对账结果并补偿】
                // 如果支付宝返回 10000（SUCCESS），说明用户在支付宝端其实已经付款成功了，只是之前因为网络波动导致回调丢包
                if (Constants.AlipayCode.SUCCESS.getCode().equals(code)) {
                    log.info("【掉单补偿成功】主动对账发现订单已付款，补发支付成功通知，orderId:{}", orderId);

                    // 3.1 自动将数据库订单状态修改为 PAY_SUCCESS，并通过 EventBus 广播微信通知
                    orderService.changeOrderPaySuccess(orderId);
                }
            }
        } catch (Exception e) {
            // 捕获异常，保证单次轮询失败不会导致整个定时任务调度线程崩溃
            log.error("【掉单补偿任务】检测未接收到支付回调通知失败", e);
        }
    }
}
