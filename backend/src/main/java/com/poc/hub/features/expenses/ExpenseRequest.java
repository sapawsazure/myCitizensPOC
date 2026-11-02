package com.poc.hub.features.expenses;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpenseRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 200) String description,
        @NotNull @DecimalMin(value = "0.01", message = "must be at least 0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull LocalDate spentOn) {
}
