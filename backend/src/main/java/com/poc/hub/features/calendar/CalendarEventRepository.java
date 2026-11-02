package com.poc.hub.features.calendar;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    /** Events that overlap the half-open interval [from, to). */
    @Query("select e from CalendarEvent e where e.startsAt < :to and e.endsAt > :from order by e.startsAt, e.id")
    List<CalendarEvent> findOverlapping(@Param("from") Instant from, @Param("to") Instant to);
}
