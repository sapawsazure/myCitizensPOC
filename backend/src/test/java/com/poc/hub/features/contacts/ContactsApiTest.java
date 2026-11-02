package com.poc.hub.features.contacts;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
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

class ContactsApiTest extends AbstractApiTest {

    @Test
    void createSearchUpdateAndDelete() throws Exception {
        MvcResult created = mvc.perform(post("/api/contacts").contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Zelda Quokka\",\"email\":\"zelda@quokka-labs.example\","
                                + "\"phone\":\"555-0100\",\"company\":\"Quokka Labs\",\"notes\":\"met at the fair\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Zelda Quokka"))
                .andReturn();
        long id = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        // case-insensitive match on name, email and company
        for (String q : new String[] {"zelda", "QUOKKA-LABS", "quokka labs"}) {
            mvc.perform(get("/api/contacts").param("q", q))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id", hasItem((int) id)));
        }
        mvc.perform(get("/api/contacts").param("q", "no-such-person-anywhere"))
                .andExpect(jsonPath("$[*].id", not(hasItem((int) id))));
        mvc.perform(get("/api/contacts"))
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        mvc.perform(put("/api/contacts/" + id).contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Zelda Q. Updated\",\"company\":\"Other Corp\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Zelda Q. Updated"))
                .andExpect(jsonPath("$.email", nullValue()));

        mvc.perform(delete("/api/contacts/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/contacts/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void blankOptionalFieldsAreStoredAsNull() throws Exception {
        MvcResult created = mvc.perform(post("/api/contacts").contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Only A Name\",\"email\":\"\",\"phone\":\"\",\"company\":\" \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", nullValue()))
                .andExpect(jsonPath("$.phone", nullValue()))
                .andExpect(jsonPath("$.company", nullValue()))
                .andReturn();
        long id = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();
        mvc.perform(delete("/api/contacts/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void percentSignInASearchIsMatchedLiterally() throws Exception {
        MvcResult created = mvc.perform(post("/api/contacts").contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Discount 100% Ltd\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long id = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(get("/api/contacts").param("q", "100%"))
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
        mvc.perform(get("/api/contacts").param("q", "%"))
                .andExpect(jsonPath("$[*].fullName", not(hasItem("Asha Verma"))));

        mvc.perform(delete("/api/contacts/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        mvc.perform(post("/api/contacts").contentType(APPLICATION_JSON).content("{\"fullName\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists());
        mvc.perform(post("/api/contacts").contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Bad Mail\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
        // an email that is present (even only spaces) must be valid; an empty string means "not given"
        mvc.perform(post("/api/contacts").contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Spaces Mail\",\"email\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
        mvc.perform(put("/api/contacts/987654321").contentType(APPLICATION_JSON).content("{\"fullName\":\"Ghost\"}"))
                .andExpect(status().isNotFound());
    }
}
