package com.Oracle.TelegramService.data.tasks;

import java.util.Date;

public record SprintResponse(
        Long sprintId,
        String name,
        Date startDate,
        Date endDate,
        String status
) {
}
