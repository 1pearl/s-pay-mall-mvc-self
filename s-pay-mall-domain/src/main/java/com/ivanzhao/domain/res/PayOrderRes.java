package com.ivanzhao.domain.res;

import com.ivanzhao.common.contants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description TODO
 * @Author IvanZhao
 * @Date 2026/9/28
 * Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderRes {

    private String userId;
    private String orderId;
    private String payUrl;
    private Constants.OrderStatusEnum orderStatus;


}
