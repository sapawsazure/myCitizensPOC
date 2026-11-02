package com.poc.hub.features.notes;

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

class NotesApiTest extends AbstractApiTest {

    @Test
    void createSearchUpdateAndDelete() throws Exception {
        MvcResult created = mvc.perform(post("/api/notes").contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Zebra crossing\",\"body\":\"Remember the quokka\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Zebra crossing"))
                .andReturn();
        long id = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        // case-insensitive match in the body, and in the title
        mvc.perform(get("/api/notes").param("q", "QUOKKA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
        mvc.perform(get("/api/notes").param("q", "zebra"))
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
        mvc.perform(get("/api/notes").param("q", "no-such-text-anywhere"))
                .andExpect(jsonPath("$[*].id", not(hasItem((int) id))));

        mvc.perform(put("/api/notes/" + id).contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Giraffe\",\"body\":\"updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Giraffe"));

        mvc.perform(delete("/api/notes/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/notes/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void percentSignIsMatchedLiterally() throws Exception {
        MvcResult created = mvc.perform(post("/api/notes").contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Discount 50% off\",\"body\":\"x\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long id = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(get("/api/notes").param("q", "50%"))
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
        mvc.perform(get("/api/notes").param("q", "%"))
                .andExpect(jsonPath("$[*].title", not(hasItem("Welcome"))));

        mvc.perform(delete("/api/notes/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void blankTitleIsRejected() throws Exception {
        mvc.perform(post("/api/notes").contentType(APPLICATION_JSON).content("{\"title\":\"\",\"body\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }
}
