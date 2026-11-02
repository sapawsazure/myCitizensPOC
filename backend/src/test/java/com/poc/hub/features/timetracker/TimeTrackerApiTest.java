package com.poc.hub.features.timetracker;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import com.poc.hub.AbstractApiTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MvcResult;

class TimeTrackerApiTest extends AbstractApiTest {

    @Autowired
    private TimeEntryRepository entries;

    @Autowired
    private ProjectRepository projects;

    @Test
    void startStopAndTheSingleRunningTimerRule() throws Exception {
        long projectId = createProject("Timer project " + System.nanoTime());

        mvc.perform(get("/api/timetracker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running", nullValue()));

        MvcResult started = mvc.perform(post("/api/timetracker/start").contentType(APPLICATION_JSON)
                        .content("{\"projectId\":" + projectId + ",\"description\":\"Write the tests\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Write the tests"))
                .andExpect(jsonPath("$.endedAt", nullValue()))
                .andReturn();
        long entryId = ((Number) JsonPath.read(started.getResponse().getContentAsString(), "$.id")).longValue();

        // the state shows the running timer and today's entry
        mvc.perform(get("/api/timetracker"))
                .andExpect(jsonPath("$.running.id").value((int) entryId))
                .andExpect(jsonPath("$.today[*].id", hasItem((int) entryId)));

        // a second timer is a conflict while one runs
        mvc.perform(post("/api/timetracker/start").contentType(APPLICATION_JSON)
                        .content("{\"projectId\":" + projectId + "}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/timetracker/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) entryId))
                .andExpect(jsonPath("$.endedAt", notNullValue()))
                .andExpect(jsonPath("$.durationSeconds", greaterThanOrEqualTo(0)));

        // nothing left to stop
        mvc.perform(post("/api/timetracker/stop")).andExpect(status().isConflict());
        mvc.perform(get("/api/timetracker")).andExpect(jsonPath("$.running", nullValue()));

        // after stopping, a new timer can start again (and is cleaned up here)
        mvc.perform(post("/api/timetracker/start").contentType(APPLICATION_JSON)
                        .content("{\"projectId\":" + projectId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(""));
        mvc.perform(post("/api/timetracker/stop")).andExpect(status().isOk());
    }

    @Test
    void theDatabaseItselfRejectsASecondRunningTimer() {
        Long projectId = projects.save(new Project("Index project " + System.nanoTime())).getId();
        TimeEntry first = entries.saveAndFlush(new TimeEntry(projectId, "first", Instant.now(), null));
        try {
            assertThatThrownBy(() -> entries.saveAndFlush(new TimeEntry(projectId, "second", Instant.now(), null)))
                    .isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            entries.delete(first);
        }
    }

    @Test
    void summaryAddsUpSecondsPerProject() throws Exception {
        Long a = projects.save(new Project("Summary A " + System.nanoTime())).getId();
        Long b = projects.save(new Project("Summary B " + System.nanoTime())).getId();
        Instant nine = LocalDate.of(2031, 5, 10).atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant();
        List<TimeEntry> saved = entries.saveAll(List.of(
                new TimeEntry(a, "one", nine, nine.plusSeconds(5400)),
                new TimeEntry(a, "two", nine.plusSeconds(7200), nine.plusSeconds(7200 + 900)),
                new TimeEntry(b, "three", nine.plusSeconds(10800), nine.plusSeconds(10800 + 600))));
        try {
            mvc.perform(get("/api/timetracker/summary").param("date", "2031-05-10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date").value("2031-05-10"))
                    .andExpect(jsonPath("$.projects", hasSize(2)))
                    .andExpect(jsonPath("$.projects[?(@.projectId==" + a + ")].seconds", contains(6300)))
                    .andExpect(jsonPath("$.projects[?(@.projectId==" + b + ")].seconds", contains(600)))
                    .andExpect(jsonPath("$.totalSeconds").value(6900));
        } finally {
            entries.deleteAll(saved);
        }
    }

    @Test
    void projectsCanBeListedAndAdded() throws Exception {
        mvc.perform(get("/api/timetracker/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("General")));

        String name = "New project " + System.nanoTime();
        createProject(name);
        mvc.perform(post("/api/timetracker/projects").contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        mvc.perform(post("/api/timetracker/projects").contentType(APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
        mvc.perform(post("/api/timetracker/start").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.projectId").exists());
        mvc.perform(post("/api/timetracker/start").contentType(APPLICATION_JSON).content("{\"projectId\":987654321}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/timetracker/summary").param("date", "not-a-date")).andExpect(status().isBadRequest());
    }

    private long createProject(String name) throws Exception {
        MvcResult result = mvc.perform(post("/api/timetracker/projects").contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }
}
