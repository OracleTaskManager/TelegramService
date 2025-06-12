package com.Oracle.TelegramService.data.tasks;


import java.util.Date;

public record SprintUpdate(
        String name,
        Date startDate,
        Date endDate
) {
}
