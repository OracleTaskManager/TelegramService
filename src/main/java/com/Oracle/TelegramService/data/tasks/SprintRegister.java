package com.Oracle.TelegramService.data.tasks;

import java.util.Date;

public record SprintRegister(
        String name,
        Date startDate,
        Date endDate
) {
}
