package com.jenislashes.finance.service;

import com.jenislashes.common.exception.BadRequestException;
import com.jenislashes.finance.dto.CreateExpenseRequest;
import com.jenislashes.finance.dto.ExpenseResponse;
import com.jenislashes.finance.model.ExpenseCategoryRecord;
import com.jenislashes.finance.model.ExpenseRecord;
import com.jenislashes.finance.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryService expenseCategoryService;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseCategoryService expenseCategoryService) {
        this.expenseRepository = expenseRepository;
        this.expenseCategoryService = expenseCategoryService;
    }

    public List<ExpenseResponse> listExpenses(LocalDate from, LocalDate toInclusive) {
        return expenseRepository.findBetween(from, toInclusive).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        ExpenseCategoryRecord category = null;
        if (request.expenseCategoryId() != null) {
            category = expenseCategoryService.requireCategory(request.expenseCategoryId());
            if (!category.isActive()) {
                throw new BadRequestException("Inactive expense category cannot be used");
            }
        }

        ExpenseRecord record = new ExpenseRecord(
                UUID.randomUUID(),
                category != null ? category.id() : null,
                category != null ? category.name() : null,
                request.expenseDate(),
                request.description().trim(),
                request.amount(),
                normalizeNullable(request.notes()),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        expenseRepository.insert(record);
        return toResponse(record);
    }

    private ExpenseResponse toResponse(ExpenseRecord record) {
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

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
