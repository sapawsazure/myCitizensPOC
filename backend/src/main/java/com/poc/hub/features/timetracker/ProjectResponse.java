package com.poc.hub.features.timetracker;

public record ProjectResponse(Long id, String name) {

    static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getName());
    }
}
