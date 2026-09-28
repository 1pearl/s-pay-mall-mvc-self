package com.ivanzhao.domain.res;

import lombok.Data;

/**
 * @author IvanZhao
 * @description 获取接口调用凭据返回接受实体(获取Access Token DTO对象)
 * @date 2026/9/26 19:37
 *       Version 1.0
 */
@Data
public class WeixinTokenRes {
    private String access_token;
    private int expires_in;
    private String errcode;
    private String errmsg;
}
