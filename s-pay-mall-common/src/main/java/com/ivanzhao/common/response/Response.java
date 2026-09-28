package com.ivanzhao.common.response;

import com.ivanzhao.common.contants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author IvanZhao
 * @description 统一响应结果对象
 * @date 2026/9/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 7000723935764546321L;

    /**
     * 业务状态码
     */
    private String code;
    /**
     * 状态描述信息
     */
    private String info;
    /**
     * 业务数据载荷
     */
    private T data;

    /**
     * 成功响应
     * 
     * @param data 响应数据
     * @return Response对象
     */
    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info(Constants.ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    /**
     * 失败响应
     * 
     * @param code 响应码
     * @param info 响应信息
     * @return Response对象
     */
    public static <T> Response<T> fail(String code, String info) {
        return Response.<T>builder()
                .code(code)
                .info(info)
                .build();
    }

    /**
     * 失败响应（按错误枚举）
     *
     * @param responseCode 响应枚举
     * @return Response<T>
     */
    public static <T> Response<T> fail(Constants.ResponseCode responseCode) {
        return Response.<T>builder()
                .code(responseCode.getCode())
                .info(responseCode.getInfo())
                .build();
    }

}
