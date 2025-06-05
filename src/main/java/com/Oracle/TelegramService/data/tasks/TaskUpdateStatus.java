package com.Oracle.TelegramService.data.tasks;

import java.time.LocalDateTime;

public record TaskUpdateStatus(
        String status,
        LocalDateTime realDeadline,
        Integer realHours
) {
}
