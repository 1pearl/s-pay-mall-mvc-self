package com.ivanzhao.domain.res;

import com.ivanzhao.common.contants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 支付订单响应对象 (DTO/VO)，包含订单号、支付链接与订单状态
 * @Author IvanZhao
 * @Date 2026/9/28
 *       Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderRes {

    /** 用户ID */
    private String userId;
    /** 订单号 */
    private String orderId;
    /** 支付链接 */
    private String payUrl;
    /** 订单状态 */
    private Constants.OrderStatusEnum orderStatus;

}
