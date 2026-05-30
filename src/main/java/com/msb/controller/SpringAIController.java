package com.msb.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class SpringAIController {

    private final ChatClient chatClient;

    public SpringAIController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    /**
     * 跟大模型聊天
     * @param message
     * @return
     */
    @GetMapping("/chat")
    public String chatWithAI(@RequestParam(value = "message")String message){
        return chatClient.prompt().user(message).call().content();
    }

    @GetMapping("/chatMemory")
    public String chatMemory(@RequestParam(value = "cid")String cid,@RequestParam(value = "message")String message){
        return chatClient.prompt().user(message).advisors(a-> a.param(ChatMemory.CONVERSATION_ID,cid)).call().content();
    }



}
