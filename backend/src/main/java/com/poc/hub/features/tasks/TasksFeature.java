package com.poc.hub.features.tasks;

import com.poc.hub.core.feature.FeatureDescriptor;

import org.springframework.stereotype.Component;

@Component
class TasksFeature implements FeatureDescriptor {

    private final TaskRepository repository;

    TasksFeature(TaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public String key() {
        return "tasks";
    }

    @Override
    public String name() {
        return "Tasks";
    }

    @Override
    public String description() {
        return "Track to-dos with status, priority and due date.";
    }

    @Override
    public String route() {
        return "/tasks";
    }

    @Override
    public String apiPath() {
        return "/api/tasks";
    }

    @Override
    public int phase() {
        return 1;
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public long itemCount() {
        return repository.count();
    }
}
