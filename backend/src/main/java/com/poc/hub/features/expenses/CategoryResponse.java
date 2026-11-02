package com.poc.hub.features.expenses;

public record CategoryResponse(Long id, String name) {

    static CategoryResponse from(ExpenseCategory category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
