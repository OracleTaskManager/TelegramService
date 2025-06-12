package com.Oracle.TelegramService.data.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EpicResponse(
        @JsonProperty("epic_id")Long epicId,
        String title,
        String description,
        String status
) {
}
