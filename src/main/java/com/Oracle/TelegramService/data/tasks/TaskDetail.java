package com.Oracle.TelegramService.data.tasks;

public record TaskDetail(
        Long taskId,
        String taskTitle,
        Integer realHours,
        String status
) {
}
