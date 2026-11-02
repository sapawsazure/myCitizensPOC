package com.poc.hub.features.expenses;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String description,
        BigDecimal amount,
        LocalDate spentOn,
        Instant createdAt) {

    static ExpenseResponse from(Expense expense, String categoryName) {
        return new ExpenseResponse(expense.getId(), expense.getCategoryId(), categoryName,
                expense.getDescription(), expense.getAmount(), expense.getSpentOn(), expense.getCreatedAt());
    }
}
