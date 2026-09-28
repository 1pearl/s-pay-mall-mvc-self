package com.ivanzhao.controller;

import com.ivanzhao.common.contants.Constants;
import com.ivanzhao.common.response.Response;
import com.ivanzhao.service.ILoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Description TODO
 * @Author IvanZhao
 * @Date 2026/9/27
 *       Version 1.0
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/login/")
public class LoginController {

    @Resource
    private ILoginService loginService;

    /**
     * 生成微信扫码登录二维码
     * @return 微信扫码登录二维码Ticket
     */
    @GetMapping("weixin_qrcode_ticket")
    public Response<String> weixinQrCodeTicket() {
        try {
            String qrCodeTicket = loginService.createQrCodeTicket();
            log.info("生成微信扫码登录 ticket:{}", qrCodeTicket);
            return Response.success(qrCodeTicket);
        } catch (Exception e) {
            log.error("获取微信二维码Ticket失败", e);
            return Response.fail(Constants.ResponseCode.UN_ERROR);
        }
    }

    /**
     * 检测扫码登录状态
     * @param ticket
     * @return
     */
    @GetMapping("check_login")
    public Response<String> checkLogin(@RequestParam String ticket) {
        try {
            String openidToken = loginService.checkLogin(ticket);
            log.info("扫码检测登录结果 ticket:{} openidToken:{}", ticket, openidToken);
            if (StringUtils.isNotBlank(openidToken)) {
                return Response.success(openidToken);
            } else {
                return Response.fail(Constants.ResponseCode.NO_LOGIN);
            }
        } catch (Exception e) {
            log.error("扫码检测登录结果失败 ticket:{}", ticket, e);
            return Response.fail(Constants.ResponseCode.UN_ERROR);
        }
    }

}
