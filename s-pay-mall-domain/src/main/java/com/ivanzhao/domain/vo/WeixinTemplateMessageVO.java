package com.ivanzhao.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IvanZhao
 * @description 微信模板消息发送请求对象
 * @date 2026/9/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeixinTemplateMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接收者（用户）的 OpenID
     */
    private String touser;

    /**
     * 所需发送的模板消息 ID
     */
    private String template_id;

    /**
     * 点击模板消息后跳转的网页 URL
     */
    @Builder.Default
    private String url = "https://weixin.qq.com";

    /**
     * 模板中的数据内容：key -> {"value": "具体文本内容"}
     */
    @Builder.Default
    private Map<String, Map<String, String>> data = new HashMap<>();

    public WeixinTemplateMessageVO(String touser, String template_id) {
        this.touser = touser;
        this.template_id = template_id;
        this.url = "https://weixin.qq.com";
        this.data = new HashMap<>();
    }

    /**
     * 链式添加模板参数（使用枚举 Key）
     *
     * @param key   模板字段枚举
     * @param value 对应填充的值
     * @return 当前对象（支持链式连续 .put）
     */
    public WeixinTemplateMessageVO put(TemplateKey key, String value) {
        if (null == this.data) {
            this.data = new HashMap<>();
        }
        this.data.put(key.getCode(), Collections.singletonMap("value", value));
        return this;
    }

    /**
     * 链式添加模板参数（支持普通字符串 Key，方便动态扩展）
     *
     * @param keyStr 模板占位符名字
     * @param value  对应填充的值
     * @return 当前对象
     */
    public WeixinTemplateMessageVO put(String keyStr, String value) {
        if (null == this.data) {
            this.data = new HashMap<>();
        }
        this.data.put(keyStr, Collections.singletonMap("value", value));
        return this;
    }

    /**
     * 微信模板消息占位符字段枚举
     */
    @Getter
    @AllArgsConstructor
    public enum TemplateKey {
        USER("user", "用户ID/OpenID"),
        ORDER_ID("orderId", "订单编号"),
        PAY_AMOUNT("payAmount", "支付金额"),
        PAY_TIME("payTime", "支付时间"),
        PRODUCT_NAME("productName", "商品名称");

        private final String code;
        private final String desc;
    }
}
