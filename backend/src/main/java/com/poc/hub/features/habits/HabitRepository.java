package com.poc.hub.features.habits;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<Habit, Long> {

    List<Habit> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}
