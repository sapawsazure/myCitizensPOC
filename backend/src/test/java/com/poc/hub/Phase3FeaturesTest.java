package com.poc.hub;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/** Phase 3 adds Contacts, Calendar and Time Tracker: 3 + 2 + 3 = 8 features in total. */
class Phase3FeaturesTest extends AbstractApiTest {

    @Test
    void phaseThreeFeaturesAreRegistered() throws Exception {
        mvc.perform(get("/api/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.features[?(@.phase==3)].key", containsInAnyOrder("contacts", "calendar", "timetracker")));
    }

    @Test
    void theApplicationNowHasExactlyEightFeaturesInOrder() throws Exception {
        mvc.perform(get("/api/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(8))
                .andExpect(jsonPath("$.features", hasSize(8)))
                .andExpect(jsonPath("$.features[*].phase", contains(1, 1, 1, 2, 2, 3, 3, 3)))
                .andExpect(jsonPath("$.features[*].key", contains(
                        "tasks", "notes", "bookmarks", "expenses", "habits", "contacts", "calendar", "timetracker")));
    }

    @Test
    void everyFeatureListEndpointAnswers200() throws Exception {
        for (String path : new String[] {
                "/api/tasks", "/api/notes", "/api/bookmarks", "/api/expenses", "/api/habits",
                "/api/contacts", "/api/calendar", "/api/timetracker"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }
}
