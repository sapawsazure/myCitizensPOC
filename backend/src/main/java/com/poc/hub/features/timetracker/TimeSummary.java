package com.poc.hub.features.timetracker;

import java.time.LocalDate;
import java.util.List;

/** Seconds worked per project for the sessions that started on {@code date}. */
public record TimeSummary(LocalDate date, List<ProjectSeconds> projects, long totalSeconds) {

    public record ProjectSeconds(Long projectId, String project, long seconds) {
    }
}
