package com.poc.hub.features.expenses;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.poc.hub.core.error.BadRequestException;
import com.poc.hub.core.error.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenses;
    private final ExpenseCategoryRepository categories;
    private final Clock clock;

    public ExpenseService(ExpenseRepository expenses, ExpenseCategoryRepository categories, Clock clock) {
        this.expenses = expenses;
        this.categories = categories;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categories.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    /** All expenses, or only those of {@code month} when given; newest first. */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(YearMonth month) {
        List<Expense> found = month == null
                ? expenses.findAllByOrderBySpentOnDescIdDesc()
                : expenses.findBySpentOnBetweenOrderBySpentOnDescIdDesc(month.atDay(1), month.atEndOfMonth());
        Map<Long, String> names = categoryNames();
        return found.stream().map(e -> ExpenseResponse.from(e, names.get(e.getCategoryId()))).toList();
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        ExpenseCategory category = categories.findById(request.categoryId())
                .orElseThrow(() -> new BadRequestException("Unknown category " + request.categoryId()));
        Expense expense = new Expense(category.getId(), request.description().trim(),
                request.amount().setScale(2, RoundingMode.UNNECESSARY), request.spentOn(), Instant.now(clock));
        return ExpenseResponse.from(expenses.save(expense), category.getName());
    }

    @Transactional
    public void delete(Long id) {
        expenses.delete(expenses.findById(id).orElseThrow(() -> NotFoundException.of("Expense", id)));
    }

    /** Total per category plus the grand total for {@code month} (default: the current month). */
    @Transactional(readOnly = true)
    public ExpenseSummary summary(YearMonth month) {
        YearMonth target = month == null ? YearMonth.now(clock) : month;
        List<CategoryTotal> totals = expenses.totalsByCategory(target.atDay(1), target.atEndOfMonth());
        BigDecimal grandTotal = totals.stream()
                .map(CategoryTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return new ExpenseSummary(target.toString(), totals, grandTotal);
    }

    private Map<Long, String> categoryNames() {
        Map<Long, String> names = new HashMap<>();
        categories.findAll().forEach(category -> names.put(category.getId(), category.getName()));
        return names;
    }
}
