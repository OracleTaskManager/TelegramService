package com.Oracle.TelegramService.data.tasks;

public record TaskHoursDetail(
        Long taskId,
        String taskTitle,
        Integer realHours,
        Integer estimatedHours
) {
}
