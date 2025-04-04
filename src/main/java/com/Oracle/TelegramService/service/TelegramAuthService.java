package com.Oracle.TelegramService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TelegramAuthService {

    private final String botSecret;

    @Autowired
    public TelegramAuthService(@Value("${telegram.bot.secret}") String botSecret) {
        this.botSecret = botSecret;
    }

    public boolean validateRequest(String incomingSecret){
        return botSecret.equals(incomingSecret);
    }

}
