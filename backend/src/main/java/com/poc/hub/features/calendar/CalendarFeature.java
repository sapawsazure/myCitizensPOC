package com.poc.hub.features.calendar;

import com.poc.hub.core.feature.FeatureDescriptor;

import org.springframework.stereotype.Component;

@Component
class CalendarFeature implements FeatureDescriptor {

    private final CalendarEventRepository repository;

    CalendarFeature(CalendarEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public String key() {
        return "calendar";
    }

    @Override
    public String name() {
        return "Calendar";
    }

    @Override
    public String description() {
        return "Plan events and browse them by month.";
    }

    @Override
    public String route() {
        return "/calendar";
    }

    @Override
    public String apiPath() {
        return "/api/calendar";
    }

    @Override
    public int phase() {
        return 3;
    }

    @Override
    public int order() {
        return 70;
    }

    @Override
    public long itemCount() {
        return repository.count();
    }
}
