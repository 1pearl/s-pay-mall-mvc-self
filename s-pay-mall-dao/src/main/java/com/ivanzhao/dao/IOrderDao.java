package com.ivanzhao.dao;

import com.ivanzhao.domain.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description TODO
 * @Author IvanZhao
 * @Date 2026/9/28
 *       Version 1.0
 */
@Mapper
public interface IOrderDao {

    /**
     * 新增支付订单。
     * 
     * @param payOrder 待保存的支付订单
     */
    void insert(PayOrder payOrder);

    /**
     * 查询未支付的订单
     * 
     * @param payOrder 用于封装查询条件的支付订单
     * @return 未支付订单；不存在时返回 {@code null}
     */
    PayOrder queryUnPayOrder(PayOrder payOrder);

    /**
     * 更新订单的支付信息。
     * 
     * @param payOrder 包含最新支付信息的订单
     */
    void updateOrderPayInfo(PayOrder payOrder);

    /**
     * 更新订单为交易关闭状态。
     * 
     * @param orderId 商户订单号
     * @return 是否更新成功
     */
    boolean changeOrderClose(String orderId);

    /**
     * 标记订单为支付成功状态，并设置支付相关时间。
     * 
     * @param payOrderReq 包含订单号和支付时间等信息的支付订单
     */
    void changeOrderPaySuccess(PayOrder payOrderReq);
}
