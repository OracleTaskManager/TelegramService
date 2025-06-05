package com.Oracle.TelegramService.data.tasks;

public record TaskDependencyRegister(
        TaskResponse taskId,
        TaskResponse blockedByTaskId
) {
}
