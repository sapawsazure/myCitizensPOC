package com.poc.hub.features.habits;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HabitRequest(@NotBlank @Size(max = 100) String name) {
}
