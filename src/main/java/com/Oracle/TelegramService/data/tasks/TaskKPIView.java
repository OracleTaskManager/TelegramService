package com.Oracle.TelegramService.data.tasks;

import java.time.LocalDateTime;

public record TaskKPIView(
        Long userId,
        Double realHours,
        LocalDateTime realDeadline,
        String status
) {
}
