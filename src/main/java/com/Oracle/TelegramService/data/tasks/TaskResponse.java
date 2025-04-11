package com.Oracle.TelegramService.data.tasks;

import java.util.Date;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Long epicId,
        String priority,
        String status,
        String type,
        Date estimatedDeadline,
        Date realDeadline,
        int userPoints
) {
}
