package com.Oracle.TelegramService.data.tasks;

import java.util.List;

public record UserHoursDetail(
        Long userId,
        String userName,
        Integer totalHours,
        List<TaskDetail> tasks
) {
}
