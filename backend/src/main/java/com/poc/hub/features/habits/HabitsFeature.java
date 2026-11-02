package com.poc.hub.features.habits;

import com.poc.hub.core.feature.FeatureDescriptor;

import org.springframework.stereotype.Component;

@Component
class HabitsFeature implements FeatureDescriptor {

    private final HabitRepository repository;

    HabitsFeature(HabitRepository repository) {
        this.repository = repository;
    }

    @Override
    public String key() {
        return "habits";
    }

    @Override
    public String name() {
        return "Habits";
    }

    @Override
    public String description() {
        return "Build streaks by checking in daily.";
    }

    @Override
    public String route() {
        return "/habits";
    }

    @Override
    public String apiPath() {
        return "/api/habits";
    }

    @Override
    public int phase() {
        return 2;
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public long itemCount() {
        return repository.count();
    }
}
