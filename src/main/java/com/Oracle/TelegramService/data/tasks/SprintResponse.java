package com.Oracle.TelegramService.data.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public record SprintResponse(
        Long sprintId,
        String name,
        @JsonProperty("start_date")Date startDate,
        @JsonProperty("end_date")Date endDate,
        String status
) {
}
