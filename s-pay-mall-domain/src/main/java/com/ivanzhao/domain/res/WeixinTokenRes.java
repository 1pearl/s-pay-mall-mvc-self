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
    /** 接口调用凭据 */
    private String access_token;
    /** 凭据有效时间，单位：秒 */
    private int expires_in;
    /** 错误码 */
    private String errcode;
    /** 错误信息 */
    private String errmsg;
}
