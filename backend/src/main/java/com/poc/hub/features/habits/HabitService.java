package com.poc.hub.features.habits;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import com.poc.hub.core.error.BadRequestException;
import com.poc.hub.core.error.ConflictException;
import com.poc.hub.core.error.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HabitService {

    private final HabitRepository habits;
    private final HabitCheckinRepository checkins;
    private final Clock clock;

    public HabitService(HabitRepository habits, HabitCheckinRepository checkins, Clock clock) {
        this.habits = habits;
        this.checkins = checkins;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> list() {
        List<Habit> all = habits.findAllByOrderByNameAsc();
        if (all.isEmpty()) {
            return List.of();
        }
        Map<Long, Set<LocalDate>> daysByHabit = new HashMap<>();
        checkins.findByHabitIdIn(all.stream().map(Habit::getId).toList())
                .forEach(c -> daysByHabit.computeIfAbsent(c.getHabitId(), id -> new HashSet<>()).add(c.getCheckedOn()));
        LocalDate today = today();
        return all.stream()
                .map(habit -> toResponse(habit, daysByHabit.getOrDefault(habit.getId(), Set.of()), today))
                .toList();
    }

    @Transactional
    public HabitResponse create(HabitRequest request) {
        String name = request.name().trim();
        if (habits.existsByName(name)) {
            throw new ConflictException("A habit named '" + name + "' already exists");
        }
        Habit habit = habits.save(new Habit(name, Instant.now(clock)));
        return toResponse(habit, Set.of(), today());
    }

    @Transactional
    public void delete(Long id) {
        // the habit's check-ins are removed by the database (ON DELETE CASCADE)
        habits.delete(find(id));
    }

    /** Idempotent: checking in twice for the same day changes nothing. */
    @Transactional
    public HabitResponse checkIn(Long habitId, LocalDate date) {
        Habit habit = find(habitId);
        LocalDate today = today();
        LocalDate day = date == null ? today : date;
        if (day.isAfter(today)) {
            throw new BadRequestException("Cannot check in for a future date");
        }
        if (!checkins.existsByHabitIdAndCheckedOn(habitId, day)) {
            checkins.save(new HabitCheckin(habitId, day));
        }
        return toResponse(habit, daysOf(habitId), today);
    }

    @Transactional
    public void removeCheckin(Long habitId, LocalDate date) {
        find(habitId);
        checkins.deleteByHabitIdAndCheckedOn(habitId, date);
    }

    /**
     * Consecutive days with a check-in ending today, or ending yesterday while today is still open;
     * 0 when neither today nor yesterday is checked.
     */
    static int currentStreak(Set<LocalDate> checkedDays, LocalDate today) {
        LocalDate day = checkedDays.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (checkedDays.contains(day)) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }

    private HabitResponse toResponse(Habit habit, Set<LocalDate> checkedDays, LocalDate today) {
        List<HabitResponse.DayStatus> last7Days = IntStream.rangeClosed(0, 6)
                .mapToObj(offset -> today.minusDays(6L - offset))
                .map(day -> new HabitResponse.DayStatus(day, checkedDays.contains(day)))
                .toList();
        return new HabitResponse(habit.getId(), habit.getName(), habit.getCreatedAt(),
                currentStreak(checkedDays, today), checkedDays.contains(today), last7Days);
    }

    private Set<LocalDate> daysOf(Long habitId) {
        Set<LocalDate> days = new HashSet<>();
        checkins.findByHabitIdIn(List.of(habitId)).forEach(c -> days.add(c.getCheckedOn()));
        return days;
    }

    private Habit find(Long id) {
        return habits.findById(id).orElseThrow(() -> NotFoundException.of("Habit", id));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
