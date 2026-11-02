package com.poc.hub.features.tasks;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of POST and PUT. {@code status} and {@code priority} are optional (defaults: new task, priority 2). */
public record TaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        TaskStatus status,
        @Min(1) @Max(3) Integer priority,
        LocalDate dueDate) {
}
