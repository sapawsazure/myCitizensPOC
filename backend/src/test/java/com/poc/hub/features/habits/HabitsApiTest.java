package com.poc.hub.features.habits;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import com.jayway.jsonpath.JsonPath;
import com.poc.hub.AbstractApiTest;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class HabitsApiTest extends AbstractApiTest {

    @Test
    void checkInsBuildAndBreakTheStreak() throws Exception {
        LocalDate today = LocalDate.now();
        long id = createHabit("Stretch " + System.nanoTime());

        mvc.perform(get("/api/habits"))
                .andExpect(jsonPath("$[?(@.id==" + id + ")].currentStreak", contains(0)))
                .andExpect(jsonPath("$[?(@.id==" + id + ")].checkedToday", contains(false)));

        // first check-in (no body = today), then the same again: idempotent
        mvc.perform(post("/api/habits/" + id + "/checkins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedToday").value(true))
                .andExpect(jsonPath("$.currentStreak").value(1))
                .andExpect(jsonPath("$.last7Days", hasSize(7)))
                .andExpect(jsonPath("$.last7Days[6].date").value(today.toString()))
                .andExpect(jsonPath("$.last7Days[6].checked").value(true));
        mvc.perform(post("/api/habits/" + id + "/checkins").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(1));

        // the two days before today make a streak of 3
        checkIn(id, today.minusDays(1));
        mvc.perform(post("/api/habits/" + id + "/checkins").contentType(APPLICATION_JSON)
                        .content("{\"date\":\"" + today.minusDays(2) + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(3));

        // undoing today keeps the streak alive: it now ends yesterday
        mvc.perform(delete("/api/habits/" + id + "/checkins/" + today)).andExpect(status().isNoContent());
        mvc.perform(get("/api/habits"))
                .andExpect(jsonPath("$[?(@.id==" + id + ")].currentStreak", contains(2)))
                .andExpect(jsonPath("$[?(@.id==" + id + ")].checkedToday", contains(false)));

        // a missed yesterday breaks it
        mvc.perform(delete("/api/habits/" + id + "/checkins/" + today.minusDays(1))).andExpect(status().isNoContent());
        mvc.perform(get("/api/habits"))
                .andExpect(jsonPath("$[?(@.id==" + id + ")].currentStreak", contains(0)));

        mvc.perform(delete("/api/habits/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/habits/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/habits")).andExpect(jsonPath("$[*].id", not(hasItem((int) id))));
    }

    @Test
    void duplicateNameIsAConflict() throws Exception {
        String name = "Unique " + System.nanoTime();
        long id = createHabit(name);
        mvc.perform(post("/api/habits").contentType(APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/habits/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        mvc.perform(post("/api/habits").contentType(APPLICATION_JSON).content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        long id = createHabit("Future check " + System.nanoTime());
        mvc.perform(post("/api/habits/" + id + "/checkins").contentType(APPLICATION_JSON)
                        .content("{\"date\":\"" + LocalDate.now().plusDays(1) + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/habits/" + id + "/checkins/not-a-date")).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/habits/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void unknownHabitIsNotFound() throws Exception {
        mvc.perform(post("/api/habits/987654321/checkins")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/habits/987654321/checkins/2030-01-01")).andExpect(status().isNotFound());
    }

    private long createHabit(String name) throws Exception {
        MvcResult result = mvc.perform(post("/api/habits").contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentStreak").value(0))
                .andExpect(jsonPath("$.checkedToday").value(false))
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private void checkIn(long id, LocalDate date) throws Exception {
        mvc.perform(post("/api/habits/" + id + "/checkins").contentType(APPLICATION_JSON)
                        .content("{\"date\":\"" + date + "\"}"))
                .andExpect(status().isOk());
    }
}
