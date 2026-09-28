package com.ivanzhao.domain.res;

import lombok.Data;

/**
 * @author IvanZhao
 * @description 获取微信登录二维码响应对象
 * @date 2026/9/26 21:06
 *       Version 1.0
 */
@Data
public class WeixinQrCodeRes {

    private String ticket;
    private Long expire_seconds;
    private String url;

}