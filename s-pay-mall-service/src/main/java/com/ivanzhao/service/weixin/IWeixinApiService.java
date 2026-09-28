package com.ivanzhao.service.weixin;

import com.ivanzhao.domain.req.WeixinQrCodeReq;
import com.ivanzhao.domain.res.WeixinQrCodeRes;
import com.ivanzhao.domain.res.WeixinTokenRes;
import com.ivanzhao.domain.vo.WeixinTemplateMessageVO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author IvanZhao
 * @description 微信Api服务
 * @date 2026/9/26 19:30
 *       Version 1.0
 */
public interface IWeixinApiService {

    /**
     * 获取微信 access_token
     * 
     * @param AppId             用户唯一凭证
     * @param appSecret         用户唯一凭证密码
     * @param client_credential 获取access_token接口调用凭证
     * @return 响应结果
     */
    @GET("cgi-bin/token")
    Call<WeixinTokenRes> getAccessToken(@Query("appid") String AppId,
            @Query("secret") String appSecret,
            @Query("grant_type") String client_credential);

    /**
     * 获取微信登录二维码
     * 
     * @param access_token 调用接口凭证
     * @param req          请求参数
     * @return 响应结果
     */
    @POST("cgi-bin/qrcode/create")
    Call<WeixinQrCodeRes> getQrCode(@Query("access_token") String access_token,
            @Body WeixinQrCodeReq req);

    /**
     * 发送模板消息
     * 
     * @param access_token 调用接口凭证
     * @param message      请求参数
     * @return 响应结果
     */
    @POST("cgi-bin/message/template/send")
    Call<Void> sendTemplateMessage(@Query("access_token") String access_token,
            @Body WeixinTemplateMessageVO message);
}
