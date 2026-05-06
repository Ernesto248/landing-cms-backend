package com.jenislashes.finance.controller;

import com.jenislashes.finance.dto.CategoryBreakdownResponse;
import com.jenislashes.finance.dto.FinanceHistoryResponse;
import com.jenislashes.finance.dto.MonthlyFinanceSummaryResponse;
import com.jenislashes.finance.dto.RangeFinanceResponse;
import com.jenislashes.finance.service.FinanceSummaryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/finance")
public class AdminFinanceController {

    private final FinanceSummaryService financeSummaryService;

    public AdminFinanceController(FinanceSummaryService financeSummaryService) {
        this.financeSummaryService = financeSummaryService;
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlyFinanceSummaryResponse> monthlySummary(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(financeSummaryService.getMonthlySummary(year, month));
    }

    @GetMapping("/history")
    public ResponseEntity<FinanceHistoryResponse> history(@RequestParam int months) {
        return ResponseEntity.ok(financeSummaryService.getFinanceHistory(months));
    }

    @GetMapping("/range-summary")
    public ResponseEntity<RangeFinanceResponse> rangeSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(financeSummaryService.getRangeSummary(from, to));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<CategoryBreakdownResponse> categoryBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(financeSummaryService.getCategoryBreakdown(from, to));
    }
}
