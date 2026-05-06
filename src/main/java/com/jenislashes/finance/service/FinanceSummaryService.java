package com.jenislashes.finance.service;

import com.jenislashes.common.exception.BadRequestException;
import com.jenislashes.finance.dto.CategoryBreakdownResponse;
import com.jenislashes.finance.dto.CategoryEntry;
import com.jenislashes.finance.dto.DailyFinanceEntry;
import com.jenislashes.finance.dto.ExpenseResponse;
import com.jenislashes.finance.dto.FinanceHistoryMonthResponse;
import com.jenislashes.finance.dto.FinanceHistoryResponse;
import com.jenislashes.finance.dto.MonthlyFinanceSummaryResponse;
import com.jenislashes.finance.dto.RangeFinanceResponse;
import com.jenislashes.finance.model.ExpenseRecord;
import com.jenislashes.finance.repository.ExpenseRepository;
import com.jenislashes.finance.repository.FinanceSummaryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class FinanceSummaryService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Havana");

    private final FinanceSummaryRepository financeSummaryRepository;
    private final ExpenseRepository expenseRepository;

    public FinanceSummaryService(
            FinanceSummaryRepository financeSummaryRepository,
            ExpenseRepository expenseRepository
    ) {
        this.financeSummaryRepository = financeSummaryRepository;
        this.expenseRepository = expenseRepository;
    }

    public RangeFinanceResponse getRangeSummary(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("From date must be before or equal to to date");
        }

        OffsetDateTime incomeFrom = from.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime incomeTo = to.plusDays(1).atStartOfDay(DEFAULT_ZONE).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);

        BigDecimal completedIncome = financeSummaryRepository.sumCompletedIncomeBetween(incomeFrom, incomeTo);
        BigDecimal recordedExpenses = expenseRepository.sumBetween(from, to);

        List<ExpenseRecord> expenseRecords = expenseRepository.findBetween(from, to);
        List<ExpenseResponse> expenses = expenseRecords.stream().map(this::toExpenseResponse).toList();

        List<DailyFinanceEntry> incomeDays = financeSummaryRepository.dailyIncomeBetween(incomeFrom, incomeTo);

        var merged = new LinkedHashMap<LocalDate, DailyFinanceEntry>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            merged.put(cursor, new DailyFinanceEntry(cursor, BigDecimal.ZERO, BigDecimal.ZERO));
            cursor = cursor.plusDays(1);
        }

        for (var day : incomeDays) {
            var existing = merged.get(day.date());
            if (existing != null) {
                merged.put(day.date(), new DailyFinanceEntry(day.date(), day.income(), existing.expenses()));
            }
        }

        expenseRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ExpenseRecord::expenseDate,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, ExpenseRecord::amount, BigDecimal::add)
                ))
                .forEach((day, total) -> {
                    var existing = merged.get(day);
                    if (existing != null) {
                        merged.put(day, new DailyFinanceEntry(day, existing.income(), total));
                    }
                });

        return new RangeFinanceResponse(
                completedIncome,
                recordedExpenses,
                completedIncome.subtract(recordedExpenses),
                expenses,
                merged.values().stream().sorted(Comparator.comparing(DailyFinanceEntry::date)).toList()
        );
    }

    private ExpenseResponse toExpenseResponse(ExpenseRecord record) {
        return new ExpenseResponse(
                record.id(),
                record.expenseCategoryId(),
                record.expenseCategoryName(),
                record.expenseDate(),
                record.description(),
                record.amount(),
                record.notes(),
                record.createdAt()
        );
    }

    public MonthlyFinanceSummaryResponse getMonthlySummary(int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        OffsetDateTime from = monthStart.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime toExclusive = nextMonthStart.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
        BigDecimal completedIncome = financeSummaryRepository.sumCompletedIncomeBetween(from, toExclusive);
        BigDecimal recordedExpenses = expenseRepository.sumBetween(monthStart, nextMonthStart.minusDays(1));

        return new MonthlyFinanceSummaryResponse(
                year,
                month,
                completedIncome,
                recordedExpenses,
                completedIncome.subtract(recordedExpenses)
        );
    }

    public CategoryBreakdownResponse getCategoryBreakdown(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("From date must be before or equal to to date");
        }

        OffsetDateTime incomeFrom = from.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime incomeTo = to.plusDays(1).atStartOfDay(DEFAULT_ZONE).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);

        List<CategoryEntry> incomeBreakdown = financeSummaryRepository.incomeBreakdownByServiceCategory(incomeFrom, incomeTo);

        List<ExpenseRecord> expenseRecords = expenseRepository.findBetween(from, to);
        List<CategoryEntry> expenseBreakdown = expenseRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.expenseCategoryName() != null ? e.expenseCategoryName() : "Sin categoria",
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, ExpenseRecord::amount, BigDecimal::add)
                ))
                .entrySet().stream()
                .map(e -> new CategoryEntry(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.amount().compareTo(a.amount()))
                .toList();

        return new CategoryBreakdownResponse(incomeBreakdown, expenseBreakdown);
    }

    public FinanceHistoryResponse getFinanceHistory(int months) {
        if (months < 1 || months > 24) {
            throw new BadRequestException("Months must be between 1 and 24");
        }

        LocalDate currentMonthStart = LocalDate.now(DEFAULT_ZONE).withDayOfMonth(1);

        return new FinanceHistoryResponse(
                IntStream.range(0, months)
                        .mapToObj(index -> currentMonthStart.minusMonths(index))
                        .map(this::buildMonthHistory)
                        .toList()
        );
    }

    private FinanceHistoryMonthResponse buildMonthHistory(LocalDate monthStart) {
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        OffsetDateTime from = monthStart.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime toExclusive = nextMonthStart.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
        BigDecimal income = financeSummaryRepository.sumCompletedIncomeBetween(from, toExclusive);
        BigDecimal expenses = expenseRepository.sumBetween(monthStart, nextMonthStart.minusDays(1));

        return new FinanceHistoryMonthResponse(
                monthStart.getYear(),
                monthStart.getMonthValue(),
                income,
                expenses
        );
    }
}
