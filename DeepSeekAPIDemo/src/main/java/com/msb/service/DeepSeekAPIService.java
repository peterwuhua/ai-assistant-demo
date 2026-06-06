package com.msb.service;

import com.msb.controller.config.DeepSeekApiClient;
import com.msb.controller.config.StreamAIClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DeepSeekAPIService {

    private final DeepSeekApiClient client;
    private final StreamAIClient streamAIClient;

    public DeepSeekAPIService(@Qualifier(value = "client") DeepSeekApiClient client,
                              @Qualifier(value = "streamAIClient") StreamAIClient streamAIClient) {
        this.client = client;
        this.streamAIClient = streamAIClient;
    }

    public String ask(String message){
        try {
         return  client.chat(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String ask2(String message){
        try {
            return  streamAIClient.chat(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
