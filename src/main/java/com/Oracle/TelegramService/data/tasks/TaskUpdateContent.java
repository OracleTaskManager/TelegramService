package com.Oracle.TelegramService.data.tasks;

import java.time.LocalDateTime;

public record TaskUpdateContent(
        String title,
        String description,
        Long epic_id,
        String priority,
        String status,
        String type,
        LocalDateTime estimated_deadline,
        LocalDateTime real_deadline,
        Integer realHours,
        Integer estimatedHours,
        Integer user_points
) {
}
