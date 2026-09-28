package com.ivanzhao.controller;

import com.ivanzhao.common.weixin.MessageTextEntity;
import com.ivanzhao.common.weixin.SignatureUtil;
import com.ivanzhao.common.weixin.XmlUtil;
import com.ivanzhao.service.ILoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author IvanZhao
 * @description 微信公众号服务端对接控制器
 * @date 2026/9/27
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/weixin/portal/")
public class WeixinPortalController {

    @Value("${weixin.config.originalid}")
    private String originalid;

    @Value("${weixin.config.token}")
    private String token;

    @Resource
    private ILoginService loginService;

    /**
     * 微信服务器握手验签接口（GET 请求）
     */
    @GetMapping(value = "receive", produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            log.info("微信公众号验签开始 -> signature:{}, timestamp:{}, nonce:{}, echostr:{}", signature, timestamp, nonce,
                    echostr);
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }
            boolean check = SignatureUtil.check(token, signature, timestamp, nonce);
            log.info("微信公众号验签完成 -> check:{}", check);
            return check ? echostr : null;
        } catch (Exception e) {
            log.error("微信公众号验签失败 [{}, {}, {}, {}]", signature, timestamp, nonce, echostr, e);
            return null;
        }
    }

    /**
     * 接收微信公众号事件推送与用户消息（POST 请求）
     */
    @PostMapping(value = "receive", produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("openid") String openid,
            @RequestParam(name = "encrypt_type", required = false) String encType,
            @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        try {
            log.info("接收微信公众号请求 -> openid:{}, body:{}", openid, requestBody);
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
            if (null == message) {
                return "";
            }

            // 1. 处理事件推送（Event）
            if ("event".equals(message.getMsgType())) {
                String event = message.getEvent();
                String ticket = message.getTicket();

                // 场景 A：已关注用户扫码登录 (SCAN)
                if ("SCAN".equals(event) && StringUtils.isNotBlank(ticket)) {
                    loginService.saveLoginState(ticket, openid);
                    log.info("用户扫码登录成功 (已关注) -> openid:{}, ticket:{}", openid, ticket);
                    return buildMessageTextEntity(openid, "登录成功！欢迎回到商城。");
                }

                // 场景 B：未关注用户扫码关注并登录 (subscribe)
                if ("subscribe".equals(event)) {
                    // 关注事件时，若带参数二维码，微信会附带 Ticket
                    if (StringUtils.isNotBlank(ticket)) {
                        loginService.saveLoginState(ticket, openid);
                        log.info("用户扫码关注并登录成功 -> openid:{}, ticket:{}", openid, ticket);
                        return buildMessageTextEntity(openid, "感谢您的关注，登录成功！");
                    }
                    return buildMessageTextEntity(openid, "欢迎关注商城公众号！");
                }
            }

            // 2. 处理普通文本消息（用户在公众号直接发文字）
            if ("text".equals(message.getMsgType())) {
                return buildMessageTextEntity(openid, "收到您的消息：" + message.getContent());
            }

            return "";
        } catch (Exception e) {
            log.error("接收微信公众号请求失败 -> openid:{}, body:{}", openid, requestBody, e);
            return "";
        }
    }

    /**
     * 构造回复给微信的被动响应文本 XML
     */
    private String buildMessageTextEntity(String openid, String content) {
        MessageTextEntity res = new MessageTextEntity();
        res.setFromUserName(originalid);
        res.setToUserName(openid);
        res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
        res.setMsgType("text");
        res.setContent(content);
        return XmlUtil.beanToXml(res);
    }
}
