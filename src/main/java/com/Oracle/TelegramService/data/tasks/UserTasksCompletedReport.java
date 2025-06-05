package com.Oracle.TelegramService.data.tasks;

import java.util.List;

public record UserTasksCompletedReport(
        Long userId,
        String userName,
        Long sprintId,
        String sprintName,
        Integer totalTasksCompleted,
        List<CompletedTask> completedTasks
) {
}
