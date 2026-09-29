package com.ivanzhao.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.google.common.eventbus.EventBus;
import com.ivanzhao.common.contants.Constants;
import com.ivanzhao.dao.IOrderDao;
import com.ivanzhao.domain.po.PayOrder;
import com.ivanzhao.domain.req.ShopCartReq;
import com.ivanzhao.domain.res.PayOrderRes;
import com.ivanzhao.domain.vo.ProductVO;
import com.ivanzhao.service.IOrderService;
import com.ivanzhao.service.rpc.ProductRPC;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;

    @Resource
    private IOrderDao orderDao;
    @Resource
    private ProductRPC productRPC;
    @Resource
    private AlipayClient alipayClient;
    @Resource
    private EventBus eventBus;

    @Override
    public PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception {
        // 1. 查询当前用户针对该商品是否存在未完成的订单（防刷 & 掉单拦截）
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setUserId(shopCartReq.getUserId());
        payOrderReq.setProductId(shopCartReq.getProductId());
        PayOrder unpayOrder = orderDao.queryUnPayOrder(payOrderReq);

        // =========================================================================
        // 分支 1：【防重复下单】已存在正在等待支付的订单（已生成过支付表单）
        // 处理方式：直接返回已有的订单号和支付链接，不再重复调支付宝
        // =========================================================================
        if (null != unpayOrder && Constants.OrderStatusEnum.PAY_WAIT.getCode().equals(unpayOrder.getStatus())) {
            log.info("用户存在未支付订单，直接返回，userId:{}, productId:{}, orderId:{}",
                    shopCartReq.getUserId(), shopCartReq.getProductId(), unpayOrder.getOrderId());
            return PayOrderRes.builder()
                    .orderId(unpayOrder.getOrderId())
                    .payUrl(unpayOrder.getPayUrl())
                    .orderStatus(Constants.OrderStatusEnum.PAY_WAIT)
                    .build();
        }

        // =========================================================================
        // 分支 2：【掉单补偿恢复】订单已落库(CREATE)，但此前调支付宝失败或中断
        // 处理方式：复用原来的 orderId，重新向支付宝发起预下单请求
        // =========================================================================
        if (null != unpayOrder && Constants.OrderStatusEnum.CREATE.getCode().equals(unpayOrder.getStatus())) {
            log.info("发现掉单记录，复用 orderId:{}", unpayOrder.getOrderId());
            PayOrder payOrder = doPreOrder(
                    unpayOrder.getProductId(),
                    unpayOrder.getOrderId(),
                    unpayOrder.getTotalAmount(),
                    unpayOrder.getProductName());
            return PayOrderRes.builder()
                    .orderId(payOrder.getOrderId())
                    .payUrl(payOrder.getPayUrl())
                    .orderStatus(Constants.OrderStatusEnum.PAY_WAIT)
                    .build();
        }

        // =========================================================================
        // 分支 3：【全新首次下单】用户第一次购买该商品
        // 处理方式：查商品 -> 生成唯一订单号 -> 落库初始单 -> 向支付宝发起预下单
        // =========================================================================
        // 3.1 模拟 RPC 远程调用商品中心获取商品价格与详情
        ProductVO productVO = productRPC.queryProductByProductId(shopCartReq.getProductId());
        String orderId = RandomStringUtils.randomNumeric(16);

        // 3.2 初始订单落库（状态为 CREATE）
        orderDao.insert(PayOrder.builder()
                .userId(shopCartReq.getUserId())
                .productId(shopCartReq.getProductId())
                .productName(productVO.getProductName())
                .orderId(orderId)
                .orderTime(new Date())
                .totalAmount(productVO.getPrice())
                .status(Constants.OrderStatusEnum.CREATE.getCode())
                .build());

        // 3.3 请求支付宝生成支付表单，并将本地订单状态更新为 PAY_WAIT
        PayOrder payOrder = doPreOrder(productVO.getProductId(), orderId, productVO.getPrice(),
                productVO.getProductName());

        // 3.4 返回给前端
        return PayOrderRes.builder()
                .orderId(orderId)
                .payUrl(payOrder.getPayUrl())
                .orderStatus(Constants.OrderStatusEnum.PAY_WAIT)
                .build();
    }

    /**
     * 变更订单状态为支付成功，并发布支付成功事件通知
     *
     * @param orderId 商户业务订单号
     */
    @Override
    public void changeOrderPaySuccess(String orderId) {
        // 1. 组装更新请求对象，设置订单号和目标状态 PAY_SUCCESS
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setOrderId(orderId);
        payOrderReq.setStatus(Constants.OrderStatusEnum.PAY_SUCCESS.getCode());

        // 2. 更新数据库：将 pay_order 表中该订单的状态改为 PAY_SUCCESS，并记录支付时间 pay_time
        orderDao.changeOrderPaySuccess(payOrderReq);

        // 3. 【核心解耦点】通过 Guava EventBus 事件总线向全系统广播一条“支付成功”事件消息
        // 将对象转为 JSON 字符串发布出去，所有订阅了该事件的监听器（如微信模板消息推送）都会收到并处理
        eventBus.post(JSON.toJSONString(payOrderReq));
    }

    /**
     * 执行预下单：请求支付宝生成支付表单，并更新本地订单为待支付状态
     *
     * @param productId   商品ID
     * @param orderId     商户订单号（即数据库中的 orderId）
     * @param totalAmount 订单总金额（如 1.68）
     * @param productName 商品名称（展示在支付宝收银台上的标题）
     * @return 组装好 payUrl 和最新状态的 PayOrder 对象
     * @throws AlipayApiException 支付宝接口调用异常
     */
    private PayOrder doPreOrder(String productId, String orderId, BigDecimal totalAmount, String productName)
            throws AlipayApiException {

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        // =========================================================================
        // 【不是发请求！】只是把我们自己服务器的接收地址告诉支付宝：
        // “支付宝老哥，等用户付完钱后，你的服务器记得往我这个地址发一条 POST 通知！”
        // =========================================================================
        request.setNotifyUrl(notifyUrl); // 目标：我们自己的内网穿透后端接口

        // =========================================================================
        // 【不是发请求！】只是把前端跳转地址告诉支付宝：
        // “用户付完钱后，网页请自动跳到这个网址展示给用户看。”
        // =========================================================================
        request.setReturnUrl(returnUrl); // 目标：前端页面

        // 组装业务参数（订单号、金额、商品名）
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", totalAmount.toString());
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        // =========================================================================
        // 【★★ 真正发出 HTTP 请求的地方 ★★】
        // 发送方：【我们自己的后端服务】
        // 接收方：【支付宝沙箱服务端 (gatewayUrl)】
        // 内容：拿着商户私钥签名，向支付宝请求生成一个合法的支付表单 HTML
        // 结果：支付宝沙箱返回一段 HTML 字符串（form）
        // =========================================================================
        String form = alipayClient.pageExecute(request).getBody();

        // 内部本地操作：存入数据库，状态改为 PAY_WAIT
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderId(orderId);
        payOrder.setPayUrl(form);
        payOrder.setStatus(Constants.OrderStatusEnum.PAY_WAIT.getCode());
        orderDao.updateOrderPayInfo(payOrder);

        return payOrder;
    }

    /**
     * 变更订单状态为交易关闭（如超时未支付关闭订单）
     *
     * @param orderId 商户订单号
     * @return 是否变更成功
     */
    @Override
    public boolean changeOrderClose(String orderId) {
        return orderDao.changeOrderClose(orderId);
    }

}
