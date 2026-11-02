package com.poc.hub.features.timetracker;

import com.poc.hub.core.feature.FeatureDescriptor;

import org.springframework.stereotype.Component;

@Component
class TimeTrackerFeature implements FeatureDescriptor {

    private final TimeEntryRepository repository;

    TimeTrackerFeature(TimeEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public String key() {
        return "timetracker";
    }

    @Override
    public String name() {
        return "Time Tracker";
    }

    @Override
    public String description() {
        return "Time your work with a start/stop timer per project.";
    }

    @Override
    public String route() {
        return "/timetracker";
    }

    @Override
    public String apiPath() {
        return "/api/timetracker";
    }

    @Override
    public int phase() {
        return 3;
    }

    @Override
    public int order() {
        return 80;
    }

    @Override
    public long itemCount() {
        return repository.count();
    }
}
