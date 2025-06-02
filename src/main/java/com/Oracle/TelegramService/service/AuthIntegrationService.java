package com.Oracle.TelegramService.service;

import com.Oracle.TelegramService.client.AuthServiceClient;
import com.Oracle.TelegramService.data.AuthResponse;
import com.Oracle.TelegramService.data.TelegramLoginRequest;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class AuthIntegrationService {

    @Autowired
    AuthServiceClient authServiceClient;

    @Autowired
    private SessionCache sessionCache;

    @Value("${telegram.bot.secret}")
    private String botSecret;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public SendMessage handleLogin(Long chatId){
        try{
            System.out.println("=== LOGIN ATTEMPT ===");
            System.out.println("ChatId: " + chatId);
            System.out.println("Auth Service URL: " + authServiceUrl);
            System.out.println("Bot Secret configured: " + (botSecret != null && !botSecret.trim().isEmpty()));
            System.out.println("Bot Secret length: " + (botSecret != null ? botSecret.length() : "null"));
            System.out.println("Bot Secret preview: " + (botSecret != null ? botSecret.substring(0, Math.min(5, botSecret.length())) + "..." : "null"));
            System.out.println("Full URL being called: " + authServiceUrl + "/users/telegram-login");

            ResponseEntity<AuthResponse> response = authServiceClient.telegramLogin(
                    botSecret,
                    new TelegramLoginRequest(chatId)
            );

            if(response.getStatusCode().is2xxSuccessful()){
                sessionCache.save(chatId, response.getBody().jwtToken());
                return new SendMessage(
                        chatId.toString(),
                        "✅ Inicio de sesión exitoso. Tu token ha sido guardado."
                );
            } else{
                System.out.println("Login failed with status: " + response.getStatusCode());
                return new SendMessage(chatId.toString(),
                        "⚠️ Error al iniciar sesión: " + response.getStatusCode());
            }

        } catch (FeignException.Unauthorized e) {
            System.err.println("=== 401 UNAUTHORIZED ERROR ===");
            System.err.println("Status: " + e.status());
            System.err.println("Message: " + e.getMessage());
            System.err.println("Response body: " + e.contentUTF8());
            return new SendMessage(chatId.toString(),
                    "⚠️ Error 401: El bot secret no es válido o el usuario no está vinculado.");

        } catch (FeignException.Forbidden e) {
            System.err.println("=== 403 FORBIDDEN ERROR ===");
            System.err.println("Status: " + e.status());
            System.err.println("Message: " + e.getMessage());
            System.err.println("Response body: " + e.contentUTF8());
            return new SendMessage(chatId.toString(),
                    "⚠️ Error de autenticación 403. Verifica la configuración del bot.");

        } catch (FeignException e) {
            System.err.println("=== FEIGN ERROR ===");
            System.err.println("Status: " + e.status());
            System.err.println("Message: " + e.getMessage());
            System.err.println("Response body: " + e.contentUTF8());
            return new SendMessage(chatId.toString(),
                    "⚠️ Error de comunicación: " + e.status());

        } catch (Exception e) {
            System.err.println("=== UNEXPECTED ERROR ===");
            e.printStackTrace();
            return new SendMessage(chatId.toString(),
                    "⚠️ Error inesperado: " + e.getMessage());
        }
    }

    public SendMessage handleStart(Long chatId) {
        // Cambiar para que apunte a una página web en lugar del endpoint directo
        String link = String.format("%s/link-telegram?chatId=%d",
                authServiceUrl.replace("/api/auth", ""), chatId); // Asumiendo que tienes frontend

        String message = "🔗 Para vincular tu cuenta:\n\n" +
                "1. Visita: " + link + "\n" +
                "2. Ingresa tu email y contraseña\n" +
                "3. Confirma la vinculación\n\n" +
                "Luego usa /login en este chat";

        return new SendMessage(chatId.toString(), message);
    }
}