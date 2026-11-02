package com.poc.hub.features.timetracker;

import java.time.Duration;
import java.time.Instant;

/**
 * A work session. {@code endedAt} is null while the timer runs; {@code durationSeconds} is then the
 * elapsed time as of the moment of the request.
 */
public record EntryResponse(
        Long id,
        Long projectId,
        String projectName,
        String description,
        Instant startedAt,
        Instant endedAt,
        long durationSeconds) {

    static EntryResponse from(TimeEntry entry, String projectName, Instant now) {
        return new EntryResponse(entry.getId(), entry.getProjectId(), projectName, entry.getDescription(),
                entry.getStartedAt(), entry.getEndedAt(), secondsOf(entry, now));
    }

    static long secondsOf(TimeEntry entry, Instant now) {
        Instant end = entry.getEndedAt() == null ? now : entry.getEndedAt();
        return Math.max(0, Duration.between(entry.getStartedAt(), end).getSeconds());
    }
}
