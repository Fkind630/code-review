package com.DXG.sdk.utils;

import com.DXG.sdk.entity.Token;
import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/5 22:46
 **/
@Data
public class WXAccessTokenUtil {
    private static final String APPID = "wxb73fec4909af07af";
    private static final String SECRET = "bf3ede5e4e020377777e27a5ea503e03";
    private static final String GRANT_TYPE = "client_credential";
    private static final String URL_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%s&appid=%s&secret=%s";

    public static String getAccessToken() {
        try {
            String urlString = String.format(URL_TEMPLATE, GRANT_TYPE, APPID, SECRET);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Print the response
                System.out.println("Response: " + response.toString());

                Token token = JSON.parseObject(response.toString(), Token.class);

                return token.getAccess_token();
            } else {
                System.out.println("GET request failed");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
