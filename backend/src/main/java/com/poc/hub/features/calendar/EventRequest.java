package com.poc.hub.features.calendar;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code endsAt} must be after {@code startsAt} (checked in the service, answered with 400). */
public record EventRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @Size(max = 200) String location,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt) {
}
