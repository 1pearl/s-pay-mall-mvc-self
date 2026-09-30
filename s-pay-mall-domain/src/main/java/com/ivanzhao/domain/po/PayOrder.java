package com.ivanzhao.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 支付订单持久化实体对象 (PO)，对应数据库表 pay_order
 * @Author IvanZhao
 * @Date 2026/9/28
 *       Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrder {

  /** 自增主键 ID */
  private Long id;

  /** 用户唯一标识（微信 OpenID / 用户系统ID） */
  private String userId;

  /** 商品ID */
  private String productId;

  /** 商品名称 */
  private String productName;

  /** 商户系统业务订单号（全局唯一，如 16 位随机数字串，对应支付宝 out_trade_no） */
  private String orderId;

  /** 下单时间（业务创建订单的时间） */
  private Date orderTime;

  /** 订单总金额（单位：元，如 1.68） */
  private BigDecimal totalAmount;

  /**
   * 订单状态（对应 Constants.OrderStatusEnum）
   * CREATE: 创建订单
   * PAY_WAIT: 等待支付（已调用支付宝生成支付表单）
   * PAY_SUCCESS: 支付成功（收到支付宝异步回调通知）
   * DEAL_DONE: 交易完成
   * CLOSE: 超时未支付/交易关闭
   */
  private String status;

  /**
   * 支付链接 / 收银台跳转凭证
   * （在电脑网站支付中，存放的是支付宝 SDK 返回的自动提交 HTML Form 表单代码）
   */
  private String payUrl;

  /** 实际支付成功的时间（在收到支付宝异步回调确认支付成功后更新填入） */
  private Date payTime;

  /** 数据库记录创建时间 */
  private Date createTime;

  /** 数据库记录最后更新时间 */
  private Date updateTime;
}
