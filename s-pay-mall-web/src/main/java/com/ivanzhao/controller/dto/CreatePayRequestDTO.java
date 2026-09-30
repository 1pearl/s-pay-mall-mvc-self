package com.ivanzhao.controller.dto;

import lombok.Data;

/**
 * @Description 创建支付订单请求 DTO，前端传入用户ID和商品ID
 * @Author IvanZhao
 * @Date 2026/9/29
 *       Version 1.0
 */
@Data
public class CreatePayRequestDTO {
    // 用户ID 【实际产生中会通过登录模块获取，不需要透彻】
    private String userId;
    // 产品编号
    private String productId;
}
