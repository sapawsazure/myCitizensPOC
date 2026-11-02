package com.poc.hub.features.tasks;

import jakarta.validation.constraints.NotNull;

/** Body of PATCH /api/tasks/{id}/status. */
public record StatusUpdateRequest(@NotNull TaskStatus status) {
}
