package com.Oracle.TelegramService.data.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public record TaskResponse(

        @JsonProperty("taskId") Long id,
        String title,
        String description,
        Long epic_id,
        String priority,
        String status,
        String type,
        Date estimated_deadline,
        Date real_deadline,
        Integer realHours,
        Integer estimatedHours,
        Integer user_points
) {

}
