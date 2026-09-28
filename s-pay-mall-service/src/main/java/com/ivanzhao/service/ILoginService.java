package com.ivanzhao.service;

import java.io.IOException;

/**
 * @Description 登录服务接口
 * @Author IvanZhao
 * @Date 2026/9/27
 *       Version 1.0
 */
public interface ILoginService {

    /**
     * 创建二维码Ticket
     * 
     * @return 二维码Ticket
     * @throws Exception
     */
    String createQrCodeTicket() throws Exception;

    /**
     * 检查登录状态
     * 
     * @param ticket 二维码Ticket
     * @return 用户OpenId
     */
    String checkLogin(String ticket);

    /**
     * 保存登录状态
     * 
     * @param ticket 二维码Ticket
     * @param openid 用户OpenId
     */
    void saveLoginState(String ticket, String openid) throws IOException;

}
