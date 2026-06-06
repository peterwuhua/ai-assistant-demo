package com.msb.controller;

import com.msb.service.DeepSeekAPIService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final DeepSeekAPIService deepSeekAPIService;

    public ChatController(DeepSeekAPIService deepSeekAPIService) {
        this.deepSeekAPIService = deepSeekAPIService;
    }

    @PostMapping("/chat")
    public String Chat(@RequestParam(value = "message") String message){
        return deepSeekAPIService.ask2(message);
    }



}
