package com.poc.hub.features.expenses;

import com.poc.hub.core.feature.FeatureDescriptor;

import org.springframework.stereotype.Component;

@Component
class ExpensesFeature implements FeatureDescriptor {

    private final ExpenseRepository repository;

    ExpensesFeature(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Override
    public String key() {
        return "expenses";
    }

    @Override
    public String name() {
        return "Expenses";
    }

    @Override
    public String description() {
        return "Log spending and see monthly totals per category.";
    }

    @Override
    public String route() {
        return "/expenses";
    }

    @Override
    public String apiPath() {
        return "/api/expenses";
    }

    @Override
    public int phase() {
        return 2;
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public long itemCount() {
        return repository.count();
    }
}
