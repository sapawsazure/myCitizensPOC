package com.poc.hub.features.habits;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitCheckinRepository extends JpaRepository<HabitCheckin, Long> {

    List<HabitCheckin> findByHabitIdIn(Collection<Long> habitIds);

    boolean existsByHabitIdAndCheckedOn(Long habitId, LocalDate checkedOn);

    long deleteByHabitIdAndCheckedOn(Long habitId, LocalDate checkedOn);
}
