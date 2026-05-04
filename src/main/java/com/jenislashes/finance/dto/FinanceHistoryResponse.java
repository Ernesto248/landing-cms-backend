package com.jenislashes.finance.dto;

import java.util.List;

public record FinanceHistoryResponse(
        List<FinanceHistoryMonthResponse> months
) {
}
