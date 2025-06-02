package com.Oracle.TelegramService.client;

import com.Oracle.TelegramService.data.AuthResponse;
import com.Oracle.TelegramService.data.TelegramLoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthServiceClient {

    // Xd endpoint to get user info by token

    @PostMapping("/users/telegram-login")
    ResponseEntity<AuthResponse> telegramLogin(
            @RequestHeader("X-Telegram-Bot-Secret") String botSecret,
            @RequestBody TelegramLoginRequest telegramLoginRequest
    );

}
