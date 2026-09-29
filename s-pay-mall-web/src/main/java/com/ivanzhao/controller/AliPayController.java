package com.ivanzhao.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.ivanzhao.common.contants.Constants;
import com.ivanzhao.common.response.Response;
import com.ivanzhao.controller.dto.CreatePayRequestDTO;
import com.ivanzhao.domain.req.ShopCartReq;
import com.ivanzhao.domain.res.PayOrderRes;
import com.ivanzhao.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付交互与回调控制器
 *
 * @Author IvanZhao
 * @Date 2026/9/29
 *       Version 1.0
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/alipay/")
public class AliPayController {

    @Resource
    private IOrderService orderService;

    /** 支付宝公钥（用于校验回调通知是否确实由支付宝官方签名发送） */
    @Value("${alipay.alipay_public_key}")
    private String alipayKey;

    /**
     * 【接口 1】创建支付订单
     * 路径：POST /api/v1/alipay/create_pay_order
     * 作用：前端用户点击“购买”时调用，生成订单并返回支付宝收银台 HTML 表单片段
     * @param createPayRequestDTO 请求体（包含 userId, productId）
     * @return 包含支付宝 Form 表单字符串的通用响应
     */
    @PostMapping("create_pay_order")
    public Response<String> createPayOrder(@RequestBody CreatePayRequestDTO createPayRequestDTO) {
        try {
            log.info("商品下单，根据商品ID创建支付单开始，userId:{} productId:{}",
                    createPayRequestDTO.getUserId(), createPayRequestDTO.getProductId());

            // 1. 获取请求入参
            String userId = createPayRequestDTO.getUserId();
            String productId = createPayRequestDTO.getProductId();

            // 2. 调用核心订单服务：处理防重复下单、落库以及请求支付宝预下单
            PayOrderRes orderRes = orderService.createOrder(
                    ShopCartReq.builder()
                            .userId(userId)
                            .productId(productId)
                            .build());

            log.info("商品下单，根据商品ID创建支付单完成，orderId:{}", orderRes.getOrderId());
            // 3. 将支付宝生成的自动提交 Form 表单 HTML 传回前端
            return Response.success(orderRes.getPayUrl());
        } catch (Exception e) {
            log.error("商品下单，根据商品ID创建支付单失败", e);
            return Response.fail(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }
    }

    /**
     * 【接口 2】支付宝异步支付结果回调通知
     * 路径：POST /api/v1/alipay/alipay_notify_url
     * 触发方：支付宝官方服务器（经由 cpolar 内网穿透隧道）
     *
     * @param request 包含支付宝回传交易状态与签名数据的 HTTP 请求
     * @return 处理成功必须严格返回字符串 "success"，失败返回 "false"
     */
    @PostMapping("alipay_notify_url")
    public String payNotify(HttpServletRequest request) throws AlipayApiException {
        log.info("【支付宝支付回调】收到消息通知，trade_status:{}", request.getParameter("trade_status"));

        // 1. 安全判断：只有交易状态为 TRADE_SUCCESS（付款成功）才进行后续处理
        if (!"TRADE_SUCCESS".equals(request.getParameter("trade_status"))) {
            return "false";
        }

        // 2. 将 HttpServletRequest 中的所有请求参数提取并转为 Map<String, String> 格式供验签使用
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }

        // 3. 提取关键业务参数
        String tradeNo = params.get("out_trade_no"); // 我们商户系统的订单号 (orderId)
        String gmtPayment = params.get("gmt_payment"); // 买家付款时间
        String alipayTradeNo = params.get("trade_no"); // 支付宝官方交易号
        String sign = params.get("sign"); // 支付宝生成的数字签名

        // 4. 【核心安全机制】RSA2 验签：防止恶意伪造支付回调请求
        String content = AlipaySignature.getSignCheckContentV1(params);
        boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayKey, "UTF-8");
        if (!checkSignature) {
            log.error("【支付宝支付回调】验签失败！疑似非法伪造请求，params:{}", params);
            return "false";
        }

        // 5. 验签通过，打印交易详情日志
        log.info("【支付回调验签通过】交易名称: {}", params.get("subject"));
        log.info("【支付回调验签通过】交易状态: {}", params.get("trade_status"));
        log.info("【支付回调验签通过】支付宝交易凭证号: {}", alipayTradeNo);
        log.info("【支付回调验签通过】商户订单号: {}", tradeNo);
        log.info("【支付回调验签通过】交易金额: {}", params.get("total_amount"));
        log.info("【支付回调验签通过】买家在支付宝唯一id: {}", params.get("buyer_id"));
        log.info("【支付回调验签通过】买家付款时间: {}", gmtPayment);
        log.info("【支付回调验签通过】买家付款金额: {}", params.get("buyer_pay_amount"));

        // 6. 更新数据库订单状态为 PAY_SUCCESS，并通过 EventBus 异步触发发送微信公众号模板消息
        orderService.changeOrderPaySuccess(tradeNo);

        // 7. 必须返回 "success" 告知支付宝已正确处理，停止重试推送
        return "success";
    }

}
