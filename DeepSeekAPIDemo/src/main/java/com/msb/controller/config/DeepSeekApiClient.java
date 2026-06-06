package com.msb.controller.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component("client")
public class DeepSeekApiClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekApiClient.class);
    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public DeepSeekApiClient(@Value("${deepseek.api-key}") String apiKey,
                             @Value("${deepseek.base-url}") String apiUrl,
                             @Value("${deepseek.model}") String model,
                             @Value("${deepseek.connect-timeout}") long connectTimeout,
                             @Value("${deepseek.read-timeout}") long readTimeout) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout,TimeUnit.SECONDS).build();
    }

    /**
     * 流式调用
     * @param userMessage
     * @return
     */
    public String chat(String userMessage) throws IOException {
        //先构建messages数组
        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.put("role","user");
        userMsg.put("content",userMessage);
        messages.add(userMsg);

        //构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model",model);
        requestBody.put("messages",messages);
        requestBody.put("temperature",0.6);
        requestBody.put("stream",true);

        //创建request请求
        RequestBody body = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.get("application/json;charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer "+apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();


        //发送同步请求
        try(Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                String errorBody = response.body() != null ? response.body().toString():"无响应体";
                throw new IOException("DeepSeek API 返回错误 ["+response.code()+"]: "+errorBody);
            }
            String respJson = response.body().string();
            return extractContentFromSSE(respJson);
        }
    }

    /**
     * 非流式读取使用
     * @param respJson
     * @return
     */
    private String extractContent(String respJson){
        JSONObject json = JSON.parseObject(respJson);
        return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }

    /**
     * 从 SSE 格式的流式响应中提取完整回复
     */
    private String extractContentFromSSE(String sseBody) {
        StringBuilder fullContent = new StringBuilder();
        String[] lines = sseBody.split("\n");

        for (String line : lines) {
            line = line.trim();
            // SSE 的数据行以 "data: " 开头
            if (line.startsWith("data: ")) {
                String jsonData = line.substring(6);  // 去掉 "data: " 前缀

                // 跳过结束标志 [DONE]
                if ("[DONE]".equals(jsonData.trim())) {
                    continue;
                }

                try {
                    JSONObject chunk = JSON.parseObject(jsonData);
                    JSONArray choices = chunk.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                        if (delta != null) {
                            String content = delta.getString("content");
                            if (content != null) {
                                fullContent.append(content);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 非 JSON 行，跳过
                }
            }
        }
        return fullContent.toString();
    }




}
