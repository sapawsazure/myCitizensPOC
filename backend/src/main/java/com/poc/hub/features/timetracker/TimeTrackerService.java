package com.poc.hub.features.timetracker;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.poc.hub.core.error.BadRequestException;
import com.poc.hub.core.error.ConflictException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimeTrackerService {

    private final ProjectRepository projects;
    private final TimeEntryRepository entries;
    private final Clock clock;

    public TimeTrackerService(ProjectRepository projects, TimeEntryRepository entries, Clock clock) {
        this.projects = projects;
        this.entries = entries;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TimerState state() {
        LocalDate today = LocalDate.now(clock);
        Instant now = Instant.now(clock);
        Map<Long, String> names = projectNames();
        EntryResponse running = entries.findFirstByEndedAtIsNull()
                .map(entry -> EntryResponse.from(entry, names.get(entry.getProjectId()), now))
                .orElse(null);
        List<EntryResponse> todays = entriesStartedOn(today).stream()
                .map(entry -> EntryResponse.from(entry, names.get(entry.getProjectId()), now))
                .toList();
        return new TimerState(today, running, todays);
    }

    /** Starts a timer. Only one may run at a time: a second start is a conflict (409). */
    @Transactional
    public EntryResponse start(StartRequest request) {
        Project project = projects.findById(request.projectId())
                .orElseThrow(() -> new BadRequestException("Unknown project " + request.projectId()));
        if (entries.findFirstByEndedAtIsNull().isPresent()) {
            throw new ConflictException("A timer is already running - stop it first");
        }
        Instant now = Instant.now(clock);
        String description = request.description() == null ? "" : request.description().trim();
        // The partial unique index in the database is the final guard against two concurrent starts.
        TimeEntry entry = entries.saveAndFlush(new TimeEntry(project.getId(), description, now, null));
        return EntryResponse.from(entry, project.getName(), now);
    }

    @Transactional
    public EntryResponse stop() {
        TimeEntry entry = entries.findFirstByEndedAtIsNull()
                .orElseThrow(() -> new ConflictException("No timer is running"));
        Instant now = Instant.now(clock);
        entry.stop(now);
        return EntryResponse.from(entry, projectNames().get(entry.getProjectId()), now);
    }

    /** Seconds per project for sessions that started on {@code date} (default: today); a running one counts up to now. */
    @Transactional(readOnly = true)
    public TimeSummary summary(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now(clock) : date;
        Instant now = Instant.now(clock);
        Map<Long, String> names = projectNames();
        Map<Long, Long> secondsByProject = new HashMap<>();
        for (TimeEntry entry : entriesStartedOn(day)) {
            secondsByProject.merge(entry.getProjectId(), EntryResponse.secondsOf(entry, now), Long::sum);
        }
        List<TimeSummary.ProjectSeconds> rows = secondsByProject.entrySet().stream()
                .map(e -> new TimeSummary.ProjectSeconds(e.getKey(), names.get(e.getKey()), e.getValue()))
                .sorted(Comparator.comparing(TimeSummary.ProjectSeconds::project))
                .toList();
        long total = rows.stream().mapToLong(TimeSummary.ProjectSeconds::seconds).sum();
        return new TimeSummary(day, rows, total);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projects.findAllByOrderByNameAsc().stream().map(ProjectResponse::from).toList();
    }

    @Transactional
    public ProjectResponse addProject(ProjectRequest request) {
        String name = request.name().trim();
        if (projects.existsByName(name)) {
            throw new ConflictException("A project named '" + name + "' already exists");
        }
        return ProjectResponse.from(projects.save(new Project(name)));
    }

    private List<TimeEntry> entriesStartedOn(LocalDate day) {
        Instant from = day.atStartOfDay(clock.getZone()).toInstant();
        Instant to = day.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        return entries.findByStartedAtGreaterThanEqualAndStartedAtLessThanOrderByStartedAtDescIdDesc(from, to);
    }

    private Map<Long, String> projectNames() {
        Map<Long, String> names = new HashMap<>();
        projects.findAll().forEach(project -> names.put(project.getId(), project.getName()));
        return names;
    }
}
