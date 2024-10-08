package com.DXG.sdk.test;

import com.DXG.sdk.entity.request.AiRequest;
import com.DXG.sdk.entity.request.Prompt;
import com.DXG.sdk.entity.response.AiResponse;
import com.DXG.sdk.enums.ModelEnum;
import com.DXG.sdk.utils.BearerTokenUtil;
import com.alibaba.fastjson2.JSON;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 21:12
 **/

public class ApiTest {

    @Test
    public void test_http() throws IOException {
        String apiKeySecret = "03b8210062925b6ece762a596e50b38b.khzXJVdQOCm0soGO";
        String token = BearerTokenUtil.getToken(apiKeySecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        String code = "1+1";

        AiRequest aiRequest = new AiRequest();
        aiRequest.setModel(ModelEnum.GLM_4.getCode());
        ArrayList<Prompt> list = new ArrayList<>();
        list.add(new Prompt("user","你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为:" + code));
        aiRequest.setMessages(list);


        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(aiRequest).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }


        try(OutputStream os = connection.getOutputStream()){
            System.out.println(JSON.toJSONString(aiRequest));
            byte[] input = JSON.toJSONString(aiRequest).getBytes();
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        //读取输出的结果
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null){
            content.append(inputLine);
        }

        System.out.println(content);

        in.close();
        connection.disconnect();

        AiResponse response = JSON.parseObject(content.toString(), AiResponse.class);
        System.out.println(response.getChoices().get(0).getMessage().getContent());


    }
}