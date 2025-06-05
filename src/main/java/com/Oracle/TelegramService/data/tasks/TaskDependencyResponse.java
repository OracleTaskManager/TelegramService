package com.Oracle.TelegramService.data.tasks;

import java.time.LocalDateTime;

public record TaskDependencyResponse(
        Long taskDependencyId,
        TaskResponse task,
        TaskResponse blockedByTask,
        LocalDateTime createdAt
) {
}
