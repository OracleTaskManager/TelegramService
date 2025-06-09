package com.Oracle.TelegramService;

import com.Oracle.TelegramService.service.TelegramCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBotHandler extends TelegramLongPollingBot {

    @Autowired
    private TelegramCommandService telegramCommandService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            try {
                // Delegar todo el procesamiento al TelegramCommandService
                SendMessage response = telegramCommandService.processCommand(chatId, messageText);
                execute(response);

            } catch (TelegramApiException e) {
                System.err.println("Error al procesar comando: " + e.getMessage());
                sendTextMessage(chatId, "⚠️ Error al procesar tu solicitud");
                System.out.println("test"); //this line
            }
        }
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}