package com.DXG.sdk.entity.response;

import java.util.List;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 21:44
 **/
public class AiResponse {
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    @Override
    public String toString() {
        return "AiResponse{" +
                "choices=" + choices +
                '}';
    }
}
