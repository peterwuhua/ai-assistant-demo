package com.msb.controller.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configure {


    @Bean
    public ChatMemory chatMemory(){
        // 返回chatMemory类型的Bean
        // 默认保留系统最多20条消息不被清除
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory memory){
        //通过ChatModel和ChatMemory来构建ChatClient实例
        return ChatClient.builder(chatModel).defaultSystem("你是一个智能聊天助手").defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();
    }

}
