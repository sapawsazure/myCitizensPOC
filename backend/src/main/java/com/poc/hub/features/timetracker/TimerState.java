package com.poc.hub.features.timetracker;

import java.time.LocalDate;
import java.util.List;

/** Response of GET /api/timetracker: the running timer (or null) and today's sessions, newest first. */
public record TimerState(LocalDate date, EntryResponse running, List<EntryResponse> today) {
}
