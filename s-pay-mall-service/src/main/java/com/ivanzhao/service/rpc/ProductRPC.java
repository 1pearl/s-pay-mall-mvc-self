package com.ivanzhao.service.rpc;

import com.ivanzhao.domain.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @Description 模拟商品中心 RPC 远程服务调用客户端
 * @Author IvanZhao
 * @Date 2026/9/28
 *       Version 1.0
 */
@Slf4j
@Service
public class ProductRPC {

    public ProductVO queryProductByProductId(String productId){
        ProductVO productVO = new ProductVO();
        productVO.setProductId(productId);
        productVO.setProductName("测试商品");
        productVO.setProductDesc("这是一个测试商品");
        productVO.setPrice(new BigDecimal("1.68"));
        return productVO;
    }

}
