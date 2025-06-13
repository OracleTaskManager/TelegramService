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

                // Si el mensaje es demasiado largo, usar el método de división
                if (response.getText().length() > 4096) {
                    sendLongMessage(chatId, response.getText());
                } else {
                    execute(response);
                }

            } catch (TelegramApiException e) {
                System.err.println("Error al procesar comando: " + e.getMessage());
                sendTextMessage(chatId, "⚠️ Error al procesar tu solicitud");
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

    private void sendLongMessage(Long chatId, String fullMessage) {
        int maxLength = 4096; // Límite de caracteres de Telegram

        for (int i = 0; i < fullMessage.length(); i += maxLength) {
            String part = fullMessage.substring(i, Math.min(fullMessage.length(), i + maxLength));
            SendMessage message = new SendMessage(chatId.toString(), part);
            message.setParseMode("Markdown");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                System.err.println("Error enviando mensaje: " + e.getMessage());
            }
        }
    }

}