package com.poc.hub.features.expenses;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import com.poc.hub.AbstractApiTest;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class ExpensesApiTest extends AbstractApiTest {

    @Test
    void createListAndDelete() throws Exception {
        long food = categoryId("Food");
        long id = createExpense(food, "Test coffee", "3.50", "2031-02-10");

        mvc.perform(get("/api/expenses").param("month", "2031-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)))
                .andExpect(jsonPath("$[?(@.id==" + id + ")].categoryName", hasItem("Food")))
                .andExpect(jsonPath("$[?(@.id==" + id + ")].amount", hasItem(3.5)));
        mvc.perform(get("/api/expenses").param("month", "2031-03"))
                .andExpect(jsonPath("$[*].id", not(hasItem((int) id))));
        mvc.perform(get("/api/expenses"))
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        mvc.perform(delete("/api/expenses/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/expenses/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void summaryTotalsPerCategoryAndGrandTotal() throws Exception {
        long food = categoryId("Food");
        long transport = categoryId("Transport");
        List<Long> created = new ArrayList<>();
        try {
            created.add(createExpense(food, "Lunch", "10.10", "2031-03-05"));
            created.add(createExpense(food, "Dinner", "5.15", "2031-03-20"));
            created.add(createExpense(transport, "Bus", "7.00", "2031-03-07"));
            created.add(createExpense(food, "Outside the month", "100.00", "2031-04-01"));

            mvc.perform(get("/api/expenses/summary").param("month", "2031-03"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.month").value("2031-03"))
                    .andExpect(jsonPath("$.categories", hasSize(2)))
                    .andExpect(jsonPath("$.categories[0].category").value("Food"))
                    .andExpect(jsonPath("$.categories[0].total").value(15.25))
                    .andExpect(jsonPath("$.categories[1].category").value("Transport"))
                    .andExpect(jsonPath("$.categories[1].total").value(7.0))
                    .andExpect(jsonPath("$.grandTotal").value(22.25));
        } finally {
            for (long id : created) {
                mvc.perform(delete("/api/expenses/" + id));
            }
        }
    }

    @Test
    void summaryOfAnEmptyMonthIsZero() throws Exception {
        mvc.perform(get("/api/expenses/summary").param("month", "2032-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(0)))
                .andExpect(jsonPath("$.grandTotal").value(0.0));
    }

    @Test
    void categoriesAreSeeded() throws Exception {
        mvc.perform(get("/api/expenses/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Food")))
                .andExpect(jsonPath("$[*].name", hasItem("Other")));
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        long food = categoryId("Food");
        mvc.perform(post("/api/expenses").contentType(APPLICATION_JSON).content(body(food, "Zero", "0", "2031-02-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
        mvc.perform(post("/api/expenses").contentType(APPLICATION_JSON).content(body(food, "Too precise", "1.234", "2031-02-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
        mvc.perform(post("/api/expenses").contentType(APPLICATION_JSON).content(body(food, " ", "5.00", "2031-02-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").exists());
        mvc.perform(post("/api/expenses").contentType(APPLICATION_JSON).content(body(987654321L, "Ghost category", "5.00", "2031-02-10")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/expenses").param("month", "2031-13")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/expenses/summary").param("month", "not-a-month")).andExpect(status().isBadRequest());
    }

    private long createExpense(long categoryId, String description, String amount, String spentOn) throws Exception {
        MvcResult result = mvc.perform(post("/api/expenses").contentType(APPLICATION_JSON)
                        .content(body(categoryId, description, amount, spentOn)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long categoryId(String name) throws Exception {
        String json = mvc.perform(get("/api/expenses/categories")).andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(json, "$[?(@.name=='" + name + "')].id");
        return ids.get(0).longValue();
    }

    private static String body(long categoryId, String description, String amount, String spentOn) {
        return "{\"categoryId\":" + categoryId + ",\"description\":\"" + description + "\",\"amount\":" + amount
                + ",\"spentOn\":\"" + spentOn + "\"}";
    }
}
