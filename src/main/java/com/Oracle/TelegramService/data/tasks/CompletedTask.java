package com.Oracle.TelegramService.data.tasks;

import java.time.LocalDateTime;

public record CompletedTask(
        Long taskId,
        String taskTitle,
        LocalDateTime completionDate,
        Integer realHours
) {
}
