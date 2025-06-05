package com.Oracle.TelegramService.data.tasks;

import java.util.List;

public record UserSprintHoursReport(
        Long userId,
        String userName,
        Long sprintId,
        String sprintName,
        Integer totalHours,
        List<TaskDetail> tasks
) {
}
