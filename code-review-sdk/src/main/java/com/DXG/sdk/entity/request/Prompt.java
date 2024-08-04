package com.DXG.sdk.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 22:03
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {
    private String role;
    private String content;
}
