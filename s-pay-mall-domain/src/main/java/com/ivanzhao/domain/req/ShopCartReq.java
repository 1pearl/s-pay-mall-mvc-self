package com.ivanzhao.domain.req;

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
@AllArgsConstructor
@NoArgsConstructor
public class ShopCartReq {

    private String userId;
    private String productId;

}
