package com.poc.hub.features.bookmarks;

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

class BookmarksApiTest extends AbstractApiTest {

    @Test
    void createFilterByCategoryUpdateAndDelete() throws Exception {
        MvcResult created = mvc.perform(post("/api/bookmarks").contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Example\",\"url\":\"https://example.org\",\"category\":\"TestCat\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("TestCat"))
                .andReturn();
        long id = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(get("/api/bookmarks").param("category", "TestCat"))
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
        mvc.perform(get("/api/bookmarks").param("category", "Other"))
                .andExpect(jsonPath("$[*].id", not(hasItem((int) id))));
        mvc.perform(get("/api/bookmarks/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("TestCat")));

        mvc.perform(put("/api/bookmarks/" + id).contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Example 2\",\"url\":\"http://example.org/two\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Example 2"))
                .andExpect(jsonPath("$.category").value("General"));

        mvc.perform(delete("/api/bookmarks/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/bookmarks/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void urlMustStartWithHttpOrHttps() throws Exception {
        mvc.perform(post("/api/bookmarks").contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Bad\",\"url\":\"ftp://example.org\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.url").exists());
        mvc.perform(post("/api/bookmarks").contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Bad\",\"url\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingTitleIsRejected() throws Exception {
        mvc.perform(post("/api/bookmarks").contentType(APPLICATION_JSON).content("{\"url\":\"https://example.org\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }
}
