package com.Oracle.TelegramService.data.tasks;

import java.util.Date;

public record TaskRegister(
        String title,
        String description,
        Long epicId,
        Priority priority,
        Type type,
        Date estimatedDeadline,
        Date realDeadline,
        int userPoints,
        int estimatedHours,
        int realHours
) {
}
