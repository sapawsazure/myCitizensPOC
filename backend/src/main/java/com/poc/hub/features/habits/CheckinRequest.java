package com.poc.hub.features.habits;

import java.time.LocalDate;

/** Optional body of POST /api/habits/{id}/checkins; without a date the check-in is for today. */
public record CheckinRequest(LocalDate date) {
}
