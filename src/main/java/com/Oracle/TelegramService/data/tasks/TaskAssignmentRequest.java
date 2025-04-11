package com.Oracle.TelegramService.data.tasks;

public record TaskAssignmentRequest(
        Long taskId,
        Long userId
) {
}
