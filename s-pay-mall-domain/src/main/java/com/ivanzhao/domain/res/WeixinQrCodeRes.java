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

    /** 换取二维码的凭证 */
    private String ticket;
    /** 二维码有效时间 */
    private Long expire_seconds;
    /** 二维码地址 */
    private String url;

}