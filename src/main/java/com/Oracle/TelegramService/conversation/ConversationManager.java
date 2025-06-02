package com.Oracle.TelegramService.conversation;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class ConversationManager {

    private final Map<Long, ConversationState> activeConversations = new ConcurrentHashMap<>();

    public void startConversation(Long chatId, String action) {
        activeConversations.put(chatId, new ConversationState(action));
    }

    public ConversationState getConversation(Long chatId) {
        return activeConversations.get(chatId);
    }

    public boolean hasActiveConversation(Long chatId) {
        return activeConversations.containsKey(chatId);
    }

    public void endConversation(Long chatId) {
        activeConversations.remove(chatId);
    }

    public void cancelConversation(Long chatId) {
        activeConversations.remove(chatId);
    }
}