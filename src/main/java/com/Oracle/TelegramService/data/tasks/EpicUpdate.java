package com.Oracle.TelegramService.data.tasks;

public record EpicUpdate(
        Long epicId,
        String title,
        String description,
        String status
) {
}
