package com.DXG.sdk.entity.request;

import com.DXG.sdk.enums.ModelEnum;

import java.util.List;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 21:59
 **/
public class AiRequest {
    private String model = ModelEnum.GLM_4.getCode();
    private List<Prompt> messages;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Prompt> getMessages() {
        return messages;
    }

    public void setMessages(List<Prompt> messages) {
        this.messages = messages;
    }

}
