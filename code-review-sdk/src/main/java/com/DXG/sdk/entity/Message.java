package com.DXG.sdk.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 22:01
 **/
@Data
public class Message {
    private String touser = "oNKsQ69LWDR3Kwm-hCPF9fsY6u8E";
    private String template_id = "gqUFDYr1EFMWK5d_0JWpnUaEo7OnAAXo_sUiohzyy-c";
    private String url = "xxx";
    private Map<String, Map<String, String>> data = new HashMap<>();

    public void put(String key, String value) {
        data.put(key, new HashMap<String, String>() {
            private static final long serialVersionUID = 7092338402387318563L;

            {
                put("value", value);
            }
        });
    }
}
