package com.poc.hub;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/** Phase 1 delivers exactly Tasks, Notes and Bookmarks (later phases add their own test classes). */
class Phase1FeaturesTest extends AbstractApiTest {

    @Test
    void phaseOneFeaturesAreRegistered() throws Exception {
        mvc.perform(get("/api/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.features[?(@.phase==1)].key", containsInAnyOrder("tasks", "notes", "bookmarks")))
                .andExpect(jsonPath("$.count", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.appVersion").isNotEmpty());
    }

    @Test
    void featuresAreSortedAndCarryLiveItemCounts() throws Exception {
        mvc.perform(get("/api/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.features[0].key").value("tasks"))
                .andExpect(jsonPath("$.features[1].key").value("notes"))
                .andExpect(jsonPath("$.features[2].key").value("bookmarks"))
                .andExpect(jsonPath("$.features[0].itemCount", greaterThan(0)));
    }

    @Test
    void everyFeatureListEndpointAnswers200() throws Exception {
        for (String path : new String[] {"/api/tasks", "/api/notes", "/api/bookmarks"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }
}
