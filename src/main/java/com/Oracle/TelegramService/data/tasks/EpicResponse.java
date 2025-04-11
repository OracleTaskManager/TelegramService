package com.Oracle.TelegramService.data.tasks;

public record EpicResponse(
        Long epicId,
        String title,
        String description,
        String status
) {
}
