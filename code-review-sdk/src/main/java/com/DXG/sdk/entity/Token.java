package com.DXG.sdk.entity;

import lombok.Data;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/5 22:49
 **/
@Data
public class Token {
    private String access_token;
    private Integer expires_in;
}
