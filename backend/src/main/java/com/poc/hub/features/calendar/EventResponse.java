package com.poc.hub.features.calendar;

import java.time.Instant;

public record EventResponse(
        Long id,
        String title,
        String description,
        String location,
        Instant startsAt,
        Instant endsAt) {

    static EventResponse from(CalendarEvent event) {
        return new EventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getLocation(),
                event.getStartsAt(), event.getEndsAt());
    }
}
