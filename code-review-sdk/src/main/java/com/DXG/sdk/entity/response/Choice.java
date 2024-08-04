package com.DXG.sdk.entity.response;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 22:19
 **/
public class Choice {
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Choice{" +
                "message=" + message +
                '}';
    }
}
