package com.poc.hub.features.tasks;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.poc.hub.AbstractApiTest;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class TasksApiTest extends AbstractApiTest {

    @Test
    void createListUpdateAndDelete() throws Exception {
        MvcResult created = mvc.perform(post("/api/tasks").contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Write tests\",\"description\":\"cover CRUD\",\"priority\":1,\"dueDate\":\"2030-01-15\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value(1))
                .andExpect(jsonPath("$.dueDate").value("2030-01-15"))
                .andReturn();
        long id = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        mvc.perform(put("/api/tasks/" + id).contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Write more tests\",\"status\":\"IN_PROGRESS\",\"priority\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Write more tests"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mvc.perform(patch("/api/tasks/" + id + "/status").contentType(APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        mvc.perform(get("/api/tasks").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
        mvc.perform(get("/api/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem((int) id))));

        mvc.perform(delete("/api/tasks/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/tasks/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void blankTitleIsRejectedWithFieldError() throws Exception {
        mvc.perform(post("/api/tasks").contentType(APPLICATION_JSON).content("{\"title\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void invalidStatusIsRejected() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "BOGUS")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/tasks/1/status").contentType(APPLICATION_JSON).content("{\"status\":\"BOGUS\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownTaskIsNotFound() throws Exception {
        mvc.perform(put("/api/tasks/999999999").contentType(APPLICATION_JSON).content("{\"title\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
}
