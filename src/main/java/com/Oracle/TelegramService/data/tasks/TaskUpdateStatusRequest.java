package com.Oracle.TelegramService.data.tasks;

public record TaskUpdateStatusRequest(
        Long taskId,
        String status
) {
}
