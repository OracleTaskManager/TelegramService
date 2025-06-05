package com.Oracle.TelegramService.data.tasks;

import java.util.List;

public record TeamSprintHoursReport(
        Long teamId,
        String teamName,
        Long sprintId,
        String sprintName,
        Integer totalHours,
        List<UserHoursDetail> userDetails
) {
}
