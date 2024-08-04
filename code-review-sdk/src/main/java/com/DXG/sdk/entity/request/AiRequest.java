package com.DXG.sdk.entity.request;

import com.DXG.sdk.enums.ModelEnum;
import lombok.Data;

import java.util.List;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 21:59
 **/
@Data
public class AiRequest {
    private String model = ModelEnum.GLM_4.getCode();
    private List<Prompt> messages;
}
