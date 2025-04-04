package com.Oracle.TelegramService;

import com.Oracle.TelegramService.data.AuthResponse;
import com.Oracle.TelegramService.data.TelegramLoginRequest;
import com.Oracle.TelegramService.service.SessionCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBotHandler extends TelegramLongPollingBot {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SessionCache sessionCache;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.secret}")
    private String botSecret;

    @Value("${auth.service.base.url}")
    private String authServiceBaseUrl;

    @Value("${auth.service.telegram-login-path}")
    private String telegramLoginPath;

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
            handleMessage(
                    update.getMessage().getChatId(),
                    update.getMessage().getText()
            );
        }
    }

    private void handleMessage(Long chatId, String text) {
        switch (text) {
            case "/start":
                handleStartCommand(chatId);
                break;
            case "/login":
                handleLoginCommand(chatId);
                break;
            default:
                sendTextMessage(chatId, "⚠️ Comando no reconocido. Usa /start para iniciar.");
                break;
        }
    }

    private void handleLoginCommand(Long chatId) {
        try {
            System.out.println("Chat ID handleLogin: " + chatId);
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Telegram-Bot-Secret", botSecret);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<AuthResponse> response = restTemplate.exchange(
                    authServiceBaseUrl + telegramLoginPath,
                    HttpMethod.POST,
                    new HttpEntity<>(new TelegramLoginRequest(chatId), headers),
                    AuthResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                sessionCache.save(chatId, response.getBody().jwtToken());
                sendTextMessage(chatId, "✅ Login exitoso! Ahora puedes usar los comandos protegidos.");
            }
        } catch (Exception e) {
            sendTextMessage(chatId, "⚠️ Error: " + e.getMessage());
        }
    }

    private void handleStartCommand(Long chatId) {
        String link = String.format("%s/link-telegram?chatId=%d", authServiceBaseUrl, chatId);
        String message = "🔗 Para vincular tu cuenta:\n\n" +
                "1. Visita: " + link + "\n" +
                "2. Inicia sesión\n" +
                "3. Confirma la vinculación\n\n" +
                "Luego usa /login en este chat";
        sendTextMessage(chatId, message);
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