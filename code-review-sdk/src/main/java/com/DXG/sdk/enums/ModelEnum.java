package com.DXG.sdk.enums;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 21:59
 **/
public enum ModelEnum {
    GLM_4("glm-4","适用于复杂的对话交互和深度内容创作设计的场景");

    private final String code;
    private final String info;

    ModelEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

}
