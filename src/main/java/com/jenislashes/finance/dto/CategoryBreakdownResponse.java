package com.jenislashes.finance.dto;

import java.util.List;

public record CategoryBreakdownResponse(
        List<CategoryEntry> incomeBreakdown,
        List<CategoryEntry> expenseBreakdown
) {
}
