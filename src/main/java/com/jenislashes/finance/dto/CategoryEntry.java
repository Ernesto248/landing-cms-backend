package com.jenislashes.finance.dto;

import java.math.BigDecimal;

public record CategoryEntry(
        String category,
        BigDecimal amount
) {
}
