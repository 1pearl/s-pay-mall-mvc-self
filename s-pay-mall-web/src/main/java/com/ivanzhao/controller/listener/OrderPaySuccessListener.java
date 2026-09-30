package com.ivanzhao.controller.listener;

import org.springframework.stereotype.Service;

import com.google.common.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;

/**
 * 支付成功事件监听器
 * 当支付宝回调或补偿任务确认支付成功后，由 EventBus 广播触发此监听
 */
@Slf4j
@Service
public class OrderPaySuccessListener {

    @Subscribe
    public void handleEvent(String paySuccessMessage) {
        log.info("【EventBus 监听】收到支付成功消息，开始执行后续业务（发货/微信模板消息推送等）: {}", paySuccessMessage);
    }

}
