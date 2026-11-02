package com.poc.hub.features.habits;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

/** Pure unit test of the streak rule - needs neither Spring nor Docker. */
class HabitStreakTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 6, 15);

    private static LocalDate daysAgo(int days) {
        return TODAY.minusDays(days);
    }

    @Test
    void noCheckinsMeansNoStreak() {
        assertThat(HabitService.currentStreak(Set.of(), TODAY)).isZero();
    }

    @Test
    void onlyTodayIsAStreakOfOne() {
        assertThat(HabitService.currentStreak(Set.of(daysAgo(0)), TODAY)).isEqualTo(1);
    }

    @Test
    void consecutiveDaysEndingTodayAreCounted() {
        assertThat(HabitService.currentStreak(Set.of(daysAgo(0), daysAgo(1), daysAgo(2)), TODAY)).isEqualTo(3);
    }

    @Test
    void aStreakEndingYesterdayIsStillAliveWhileTodayIsOpen() {
        assertThat(HabitService.currentStreak(Set.of(daysAgo(1), daysAgo(2), daysAgo(3)), TODAY)).isEqualTo(3);
    }

    @Test
    void missingBothTodayAndYesterdayResetsToZero() {
        assertThat(HabitService.currentStreak(Set.of(daysAgo(2), daysAgo(3), daysAgo(4)), TODAY)).isZero();
    }

    @Test
    void aGapEndsTheStreak() {
        assertThat(HabitService.currentStreak(Set.of(daysAgo(0), daysAgo(2), daysAgo(3)), TODAY)).isEqualTo(1);
    }

    @Test
    void longStreaksAcrossMonthBoundariesAreCounted() {
        Set<LocalDate> days = Set.of(daysAgo(0), daysAgo(5), daysAgo(1), daysAgo(2), daysAgo(3), daysAgo(4), daysAgo(40));
        assertThat(HabitService.currentStreak(days, TODAY)).isEqualTo(6);
    }
}
