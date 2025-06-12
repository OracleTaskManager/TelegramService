package com.Oracle.TelegramService.data.tasks;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public record SprintHoursReport(
        Long sprintId,
        String sprintName,
        Date startDate,
        Date endDate,
        Integer totalHours,
        List<TaskHoursDetail> taskDetails
) {
}
