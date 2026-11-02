package com.poc.hub.features.timetracker;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    Optional<TimeEntry> findFirstByEndedAtIsNull();

    /** Entries that started in the half-open interval [from, to), newest first. */
    List<TimeEntry> findByStartedAtGreaterThanEqualAndStartedAtLessThanOrderByStartedAtDescIdDesc(Instant from, Instant to);
}
