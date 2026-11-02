package com.poc.hub.features.habits;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "habit_checkins")
public class HabitCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "habit_id", nullable = false)
    private Long habitId;

    @Column(name = "checked_on", nullable = false)
    private LocalDate checkedOn;

    protected HabitCheckin() {
    }

    public HabitCheckin(Long habitId, LocalDate checkedOn) {
        this.habitId = habitId;
        this.checkedOn = checkedOn;
    }

    public Long getHabitId() {
        return habitId;
    }

    public LocalDate getCheckedOn() {
        return checkedOn;
    }
}
