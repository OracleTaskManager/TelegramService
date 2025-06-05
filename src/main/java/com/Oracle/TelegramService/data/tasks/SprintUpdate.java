package com.Oracle.TelegramService.data.tasks;

import java.time.LocalDateTime;

public record SprintUpdate(
        Long sprintId,
        String name,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
