package com.poc.hub.features.expenses;

import java.math.BigDecimal;
import java.util.List;

/** Response of GET /api/expenses/summary: totals per category and the grand total, all with scale 2. */
public record ExpenseSummary(String month, List<CategoryTotal> categories, BigDecimal grandTotal) {
}
