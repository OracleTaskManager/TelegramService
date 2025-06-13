package com.Oracle.TelegramService.data.tasks;


public record TaskAssignmentResponse(
        Long taskId,
        String taskTitle,
        Long userId,
        String userName
) {
}
