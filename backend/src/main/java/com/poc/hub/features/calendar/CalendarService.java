package com.poc.hub.features.calendar;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

import com.poc.hub.core.error.BadRequestException;
import com.poc.hub.core.error.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarService {

    private final CalendarEventRepository repository;
    private final Clock clock;

    public CalendarService(CalendarEventRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Events overlapping {@code month} (default: the current month), in the server's time zone. */
    @Transactional(readOnly = true)
    public List<EventResponse> list(YearMonth month) {
        YearMonth target = month == null ? YearMonth.now(clock) : month;
        Instant from = target.atDay(1).atStartOfDay(clock.getZone()).toInstant();
        Instant to = target.plusMonths(1).atDay(1).atStartOfDay(clock.getZone()).toInstant();
        return repository.findOverlapping(from, to).stream().map(EventResponse::from).toList();
    }

    @Transactional
    public EventResponse create(EventRequest request) {
        requireValidRange(request);
        CalendarEvent event = new CalendarEvent(request.title().trim(), blankToNull(request.description()),
                blankToNull(request.location()), request.startsAt(), request.endsAt());
        return EventResponse.from(repository.save(event));
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        requireValidRange(request);
        CalendarEvent event = find(id);
        event.edit(request.title().trim(), blankToNull(request.description()), blankToNull(request.location()),
                request.startsAt(), request.endsAt());
        return EventResponse.from(event);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private CalendarEvent find(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Event", id));
    }

    private static void requireValidRange(EventRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new BadRequestException("endsAt must be after startsAt");
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
