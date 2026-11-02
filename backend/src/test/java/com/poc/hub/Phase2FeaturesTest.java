package com.poc.hub;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/** Phase 2 adds exactly Expenses and Habits on top of the three Phase 1 features. */
class Phase2FeaturesTest extends AbstractApiTest {

    @Test
    void phaseTwoFeaturesAreRegistered() throws Exception {
        mvc.perform(get("/api/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.features[?(@.phase==2)].key", containsInAnyOrder("expenses", "habits")))
                .andExpect(jsonPath("$.count", greaterThanOrEqualTo(5)));
    }

    @Test
    void phaseOneFeaturesAreStillPresentAndPhaseTwoFollowsThem() throws Exception {
        mvc.perform(get("/api/features"))
                .andExpect(jsonPath("$.features[?(@.phase==1)].key", containsInAnyOrder("tasks", "notes", "bookmarks")))
                .andExpect(jsonPath("$.features[3].key").value("expenses"))
                .andExpect(jsonPath("$.features[4].key").value("habits"));
    }

    @Test
    void phaseTwoListEndpointsAnswer200() throws Exception {
        for (String path : new String[] {"/api/expenses", "/api/habits"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }
}
