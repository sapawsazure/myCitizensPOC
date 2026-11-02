package com.poc.hub.features.expenses;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Total spent in one category (used by the monthly summary and as a JPQL constructor result). */
public record CategoryTotal(String category, BigDecimal total) {

    public CategoryTotal {
        total = total.setScale(2, RoundingMode.HALF_UP);
    }
}
