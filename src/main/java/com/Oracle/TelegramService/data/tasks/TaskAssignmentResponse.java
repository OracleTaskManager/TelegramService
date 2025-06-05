package com.Oracle.TelegramService.data.tasks;

import java.time.LocalDateTime;

public record TaskAssignmentResponse(
        Long assignmentId,
        Long taskId,
        String taskTitle,
        Long userId,
        String userName,
        LocalDateTime assignedAt
) {
}
