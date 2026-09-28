package com.ivanzhao.domain.req;

import lombok.*;

/**
 * @ClassName WeixinQrCodeReq
 * @Description 微信二维码请求对象
 * @Author IvanZhao
 * @Date 2026/9/26 21:06
 *       Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeixinQrCodeReq {
    private Integer expire_seconds;
    private String action_name;
    private ActionInfo action_info;

    @Getter
    @AllArgsConstructor
    public enum ActionType {
        QR_SCENE("QR_SCENE", "临时的整型参数值"),
        QR_STR_SCENE("QR_STR_SCENE", "临时的字符串参数值"),
        QR_LIMIT_SCENE("QR_LIMIT_SCENE", "永久的整型参数值"),
        QR_LIMIT_STR_SCENE("QR_LIMIT_STR_SCENE", "永久的字符串参数值");

        private final String code;
        private final String info;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActionInfo {
        private Scene scene;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Scene {
        private Integer scene_id;
        private String scene_str;
    }

    /**
     * 快捷构造临时整型二维码
     */
    public static WeixinQrCodeReq of(int expire_seconds, int scene_id) {
        return WeixinQrCodeReq.builder()
                .expire_seconds(expire_seconds)
                .action_name(WeixinQrCodeReq.ActionType.QR_SCENE.getCode())
                .action_info(WeixinQrCodeReq.ActionInfo.builder()
                        .scene(WeixinQrCodeReq.Scene.builder().scene_id(scene_id).build())
                        .build())
                .build();
    }

    /**
     * 快捷构造临时字符串二维码
     */
    public static WeixinQrCodeReq of(int expire_seconds, String scene_str) {
        return WeixinQrCodeReq.builder()
                .expire_seconds(expire_seconds)
                .action_name(WeixinQrCodeReq.ActionType.QR_STR_SCENE.getCode())
                .action_info(WeixinQrCodeReq.ActionInfo.builder()
                        .scene(WeixinQrCodeReq.Scene.builder().scene_str(scene_str).build())
                        .build())
                .build();
    }

    /**
     * 快捷构造永久整型二维码
     */
    public static WeixinQrCodeReq of(int scene_id) {
        return WeixinQrCodeReq.builder()
                .action_name(ActionType.QR_LIMIT_SCENE.getCode())
                .action_info(ActionInfo.builder().scene(
                        Scene.builder().scene_id(scene_id).build()).build())
                .build();
    }

    /**
     * 快捷构造永久字符串二维码
     */
    public static WeixinQrCodeReq of(String scene_id) {
        return WeixinQrCodeReq.builder()
                .action_name(ActionType.QR_LIMIT_STR_SCENE.getCode())
                .action_info(ActionInfo.builder().scene(
                        Scene.builder().scene_str(scene_id).build()).build())
                .build();
    }
}
