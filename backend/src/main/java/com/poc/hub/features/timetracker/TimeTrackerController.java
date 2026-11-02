package com.poc.hub.features.timetracker;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timetracker")
public class TimeTrackerController {

    private final TimeTrackerService service;

    public TimeTrackerController(TimeTrackerService service) {
        this.service = service;
    }

    @GetMapping
    public TimerState state() {
        return service.state();
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public EntryResponse start(@Valid @RequestBody StartRequest request) {
        return service.start(request);
    }

    @PostMapping("/stop")
    public EntryResponse stop() {
        return service.stop();
    }

    @GetMapping("/summary")
    public TimeSummary summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.summary(date);
    }

    @GetMapping("/projects")
    public List<ProjectResponse> projects() {
        return service.listProjects();
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse addProject(@Valid @RequestBody ProjectRequest request) {
        return service.addProject(request);
    }
}
