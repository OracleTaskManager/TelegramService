package com.Oracle.TelegramService.data;

public record AuthResponse(
        String jwtToken
) {
    public String jwtToken(){
        return this.jwtToken;
    }
}
