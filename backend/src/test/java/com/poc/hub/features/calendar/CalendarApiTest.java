package com.poc.hub.features.calendar;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.poc.hub.AbstractApiTest;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class CalendarApiTest extends AbstractApiTest {

    @Test
    void createListByMonthUpdateAndDelete() throws Exception {
        long id = createEvent("Planning session", "2031-06-10T09:00:00Z", "2031-06-10T10:00:00Z");

        mvc.perform(get("/api/calendar").param("month", "2031-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
        mvc.perform(get("/api/calendar").param("month", "2031-07"))
                .andExpect(jsonPath("$[*].id", not(hasItem((int) id))));

        mvc.perform(put("/api/calendar/" + id).contentType(APPLICATION_JSON)
                        .content(body("Planning session v2", "2031-06-10T09:00:00Z", "2031-06-10T11:30:00Z")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Planning session v2"))
                .andExpect(jsonPath("$.endsAt").value("2031-06-10T11:30:00Z"));

        mvc.perform(delete("/api/calendar/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/calendar/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void anEventSpanningTwoMonthsAppearsInBoth() throws Exception {
        long id = createEvent("Long conference", "2031-08-25T00:00:00Z", "2031-09-05T00:00:00Z");
        try {
            mvc.perform(get("/api/calendar").param("month", "2031-08"))
                    .andExpect(jsonPath("$[*].id", hasItem((int) id)));
            mvc.perform(get("/api/calendar").param("month", "2031-09"))
                    .andExpect(jsonPath("$[*].id", hasItem((int) id)));
            mvc.perform(get("/api/calendar").param("month", "2031-10"))
                    .andExpect(jsonPath("$[*].id", not(hasItem((int) id))));
        } finally {
            mvc.perform(delete("/api/calendar/" + id));
        }
    }

    @Test
    void theEndMustBeAfterTheStart() throws Exception {
        mvc.perform(post("/api/calendar").contentType(APPLICATION_JSON)
                        .content(body("Zero length", "2031-06-10T09:00:00Z", "2031-06-10T09:00:00Z")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("after")));
        mvc.perform(post("/api/calendar").contentType(APPLICATION_JSON)
                        .content(body("Backwards", "2031-06-10T10:00:00Z", "2031-06-10T09:00:00Z")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        mvc.perform(post("/api/calendar").contentType(APPLICATION_JSON)
                        .content(body(" ", "2031-06-10T09:00:00Z", "2031-06-10T10:00:00Z")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
        mvc.perform(post("/api/calendar").contentType(APPLICATION_JSON).content("{\"title\":\"No times\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.startsAt").exists())
                .andExpect(jsonPath("$.errors.endsAt").exists());
        mvc.perform(get("/api/calendar").param("month", "2031-13")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/calendar/987654321").contentType(APPLICATION_JSON)
                        .content(body("Ghost", "2031-06-10T09:00:00Z", "2031-06-10T10:00:00Z")))
                .andExpect(status().isNotFound());
    }

    @Test
    void seededEventsAreListedForTheCurrentMonthByDefault() throws Exception {
        mvc.perform(get("/api/calendar")).andExpect(status().isOk());
    }

    private long createEvent(String title, String startsAt, String endsAt) throws Exception {
        MvcResult result = mvc.perform(post("/api/calendar").contentType(APPLICATION_JSON)
                        .content(body(title, startsAt, endsAt)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private static String body(String title, String startsAt, String endsAt) {
        return "{\"title\":\"" + title + "\",\"startsAt\":\"" + startsAt + "\",\"endsAt\":\"" + endsAt + "\"}";
    }
}
