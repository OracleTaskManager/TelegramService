package com.Oracle.TelegramService.data.tasks;

import java.time.LocalDateTime;
import java.util.List;

public record SprintHoursReport(
        Long sprintId,
        String sprintName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer totalHours,
        List<TaskHoursDetail> taskDetails
) {
}
