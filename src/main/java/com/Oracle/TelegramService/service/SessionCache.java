package com.Oracle.TelegramService.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionCache {
    private final Map<Long, String> chatIdToTokenMap = new ConcurrentHashMap<>();
    private final Map<String, Long> tokenToChatIdMap = new ConcurrentHashMap<>();

    public void save(Long chatId, String token) {
        chatIdToTokenMap.put(chatId, token);
        tokenToChatIdMap.put(token, chatId);
    }

    public String getToken(Long chatId) {
        return chatIdToTokenMap.get(chatId);
    }

    public Long getChatId(String token) {
        return tokenToChatIdMap.get(token);
    }

    public void removeByChatId(Long chatId) {
        String token = chatIdToTokenMap.remove(chatId);
        if (token != null) {
            tokenToChatIdMap.remove(token);
        }
    }
}