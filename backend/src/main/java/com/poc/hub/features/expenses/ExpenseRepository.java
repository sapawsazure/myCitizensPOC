package com.poc.hub.features.expenses;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByOrderBySpentOnDescIdDesc();

    List<Expense> findBySpentOnBetweenOrderBySpentOnDescIdDesc(LocalDate from, LocalDate to);

    @Query("""
            select new com.poc.hub.features.expenses.CategoryTotal(c.name, sum(e.amount))
            from Expense e join ExpenseCategory c on c.id = e.categoryId
            where e.spentOn between :from and :to
            group by c.name
            order by c.name
            """)
    List<CategoryTotal> totalsByCategory(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
