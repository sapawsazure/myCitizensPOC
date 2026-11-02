package com.poc.hub.features.timetracker;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body of POST /api/timetracker/start; the description is optional. */
public record StartRequest(@NotNull Long projectId, @Size(max = 200) String description) {
}
