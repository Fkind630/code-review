package com.DXG.sdk.model;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 22:03
 **/
public class Prompt {
    private String role;
    private String content;

    public Prompt() {
    }

    public Prompt(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
