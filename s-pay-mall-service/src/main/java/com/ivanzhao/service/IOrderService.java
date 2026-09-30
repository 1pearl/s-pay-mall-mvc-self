package com.ivanzhao.service;

import com.ivanzhao.domain.req.ShopCartReq;
import com.ivanzhao.domain.res.PayOrderRes;

import java.util.List;

/**
 * @Description 订单服务接口
 * @Author IvanZhao
 * @Date 2026/9/28
 *       Version 1.0
 */
public interface IOrderService {

    /**
     * 创建支付订单（含防重校验、预下单与生成收银台表单）
     *
     * @param shopCartReq 购物车下单请求对象
     * @return 订单响应（含订单号、支付表单、订单状态）
     * @throws Exception 异常
     */
    PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception;

    /**
     * 变更订单状态为支付成功，并发布广播事件
     *
     * @param tradeNo 商户订单号 (orderId)
     */
    void changeOrderPaySuccess(String tradeNo);

    /**
     * 变更订单状态为关闭（超时未支付等场景）
     *
     * @param orderId 商户订单号 (orderId)
     * @return 是否变更成功
     */
    boolean changeOrderClose(String orderId);


    /**
     * 【定时任务1】查询超时未支付订单列表（用于订单超时关单）。
     * 
     * @return 超时未支付订单的订单号列表
     */
    List<String> queryTimeOutCloseOrderList();

    /**
     * 【定时任务2】查询未收到支付通知的订单列表（用于支付通知补偿）。
     * 
     * @return 未收到支付通知的订单号列表
     */
    List<String> queryNoPayNotifyOrder();
}
