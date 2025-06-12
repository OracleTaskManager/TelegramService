package com.Oracle.TelegramService.data.tasks;

import java.util.Date;

public record CompletedTask(
        Long taskId,
        String taskTitle,
        Date completionDate,
        Integer realHours
) {
}
