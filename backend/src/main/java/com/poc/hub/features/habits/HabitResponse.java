package com.poc.hub.features.habits;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * A habit with its derived state. {@code last7Days} runs from six days ago up to today (oldest first)
 * so the UI can draw the indicator without doing any date arithmetic of its own.
 */
public record HabitResponse(
        Long id,
        String name,
        Instant createdAt,
        int currentStreak,
        boolean checkedToday,
        List<DayStatus> last7Days) {

    public record DayStatus(LocalDate date, boolean checked) {
    }
}
