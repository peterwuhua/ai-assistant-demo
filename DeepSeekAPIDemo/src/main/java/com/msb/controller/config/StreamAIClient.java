package com.msb.controller.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class StreamAIClient {

    private static final Logger log = LoggerFactory.getLogger(StreamAIClient.class);
    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public StreamAIClient(@Value("${deepseek.api-key}") String apiKey,
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
//        try(Response response = httpClient.newCall(request).execute()){
//            if (!response.isSuccessful()){
//                String errorBody = response.body() != null ? response.body().toString():"无响应体";
//                throw new IOException("DeepSeek API 返回错误 ["+response.code()+"]: "+errorBody);
//            }
//            String respJson = response.body().string();
//            return extractContentFromSSE(respJson);
//        }
        //使用 BufferedReader 逐行读取（适合真正的流式消费
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().toString():"无响应体";
                throw new IOException("DeepSeek API 返回错误 ["+response.code()+"]: "+errorBody);
            }

            BufferedReader reader = new BufferedReader(response.body().charStream());
            StringBuilder fullContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6);
                    if ("[DONE]".equals(jsonData.trim())) {
                        break;
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
                    }catch (Exception e) {
                        // 非 JSON 行，跳过
                    }
                }
            }
            return fullContent.toString();
        }

    }









}
