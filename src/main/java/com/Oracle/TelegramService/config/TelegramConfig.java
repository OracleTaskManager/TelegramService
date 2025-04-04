package com.Oracle.TelegramService.config;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public String getBotToken() {
        return botToken;
    }

}
