package com.ivanzhao.service.impl;

import com.google.common.cache.Cache;
import com.ivanzhao.domain.req.WeixinQrCodeReq;
import com.ivanzhao.domain.res.WeixinQrCodeRes;
import com.ivanzhao.domain.res.WeixinTokenRes;
import com.ivanzhao.domain.vo.WeixinTemplateMessageVO;
import com.ivanzhao.service.ILoginService;
import com.ivanzhao.service.weixin.IWeixinApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Description TODO
 * @Author IvanZhao
 * @Date 2026/9/27
 *       Version 1.0
 */
@Slf4j
@Service
public class WeixinLoginServiceImpl implements ILoginService {

    @Value("${weixin.config.app-id}")
    private String appid;

    @Value("${weixin.config.app-secret}")
    private String appSecret;

    @Value("${weixin.config.template_id}")
    private String template_id;

    @Resource
    private Cache<String, String> weixinAccessToken;

    @Resource
    private Cache<String, String> openidToken;

    @Resource
    private IWeixinApiService weixinApiService;

    @Override
    public String createQrCodeTicket() throws Exception {
        // 1. 获取 accessToken
        String accessToken = getAccessToken();
        // 2. 生成 ticket
        WeixinQrCodeReq qrCodeReq = WeixinQrCodeReq.of(2592000, 100601);
        Call<WeixinQrCodeRes> call = weixinApiService.getQrCode(accessToken, qrCodeReq);
        WeixinQrCodeRes qrCodeRes = call.execute().body();

        return qrCodeRes.getTicket();
    }

    @Override
    public String checkLogin(String ticket) {
        return openidToken.getIfPresent(ticket);
    }

    @Override
    public void saveLoginState(String ticket, String openid) throws IOException {
        // 1. 保存登录状态到 Guava 缓存（让前端轮询能立即感知登录成功）
        openidToken.put(ticket, openid);

        // 2. 拿到微信凭证（直接复用抽取好的 getAccessToken 方法，无需重复手写十几行！）
        String accessToken = getAccessToken();

        // 3. 构建模板消息对象（利用链式调用，极其清爽）
        WeixinTemplateMessageVO templateMessage = new WeixinTemplateMessageVO(openid, template_id);
        templateMessage.setUrl("https://gaga.plus"); // 模板消息点击后跳转的网页
        templateMessage.put(WeixinTemplateMessageVO.TemplateKey.USER, openid);

        // 4. 调用 Retrofit 发送微信公众号模板消息
        Call<Void> call = weixinApiService.sendTemplateMessage(accessToken, templateMessage);
        call.execute();
        log.info("扫码登录成功，已成功向用户微信推送模板消息 -> openid:{}, ticket:{}", openid, ticket);
    }

    /**
     * 获取/刷新微信 AccessToken（带 Guava 缓存机制）
     *
     * @return 有效的 AccessToken 字符串
     * @throws IOException 网络异常
     */
    private String getAccessToken() throws IOException {
        // 1. 优先从 Guava 内存缓存读取
        String accessToken = weixinAccessToken.getIfPresent(appid);
        if (StringUtils.isNotBlank(accessToken)) {
            return accessToken;
        }

        // 2. 缓存失效时，远程调用微信官方 API
        log.info("AccessToken 缓存已过期或未初始化，正在请求微信开放平台重新获取...");
        Call<WeixinTokenRes> call = weixinApiService.getAccessToken(appid, appSecret, "client_credential");
        WeixinTokenRes weixinTokenRes = call.execute().body();
        if (null == weixinTokenRes || StringUtils.isBlank(weixinTokenRes.getAccess_token())) {
            log.error("获取微信 AccessToken 失败，微信返回结果为空");
            throw new RuntimeException("获取微信 AccessToken 失败！");
        }

        // 3. 写入缓存并返回
        accessToken = weixinTokenRes.getAccess_token();
        weixinAccessToken.put(appid, accessToken);
        log.info("成功获取微信 AccessToken 并已写入 Guava 缓存");
        return accessToken;
    }
}
