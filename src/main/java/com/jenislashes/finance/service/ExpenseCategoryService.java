package com.jenislashes.finance.service;

import com.jenislashes.common.exception.NotFoundException;
import com.jenislashes.finance.dto.CreateExpenseCategoryRequest;
import com.jenislashes.finance.dto.ExpenseCategoryResponse;
import com.jenislashes.finance.model.ExpenseCategoryRecord;
import com.jenislashes.finance.repository.ExpenseCategoryRepository;
import com.jenislashes.finance.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final ExpenseRepository expenseRepository;

    public ExpenseCategoryService(
            ExpenseCategoryRepository expenseCategoryRepository,
            ExpenseRepository expenseRepository
    ) {
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.expenseRepository = expenseRepository;
    }

    public List<ExpenseCategoryResponse> listCategories() {
        return expenseCategoryRepository.findAll().stream()
                .map(category -> new ExpenseCategoryResponse(category.id(), category.name(), category.isActive()))
                .toList();
    }

    public ExpenseCategoryRecord requireCategory(UUID categoryId) {
        return expenseCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Expense category not found"));
    }

    @Transactional
    public ExpenseCategoryResponse createCategory(CreateExpenseCategoryRequest request) {
        ExpenseCategoryRecord record = new ExpenseCategoryRecord(
                UUID.randomUUID(),
                request.name().trim(),
                request.isActive(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        expenseCategoryRepository.insert(record);
        return new ExpenseCategoryResponse(record.id(), record.name(), record.isActive());
    }

    @Transactional
    public void deleteCategoryByName(String name) {
        var existing = expenseCategoryRepository.findByName(name);
        if (existing.isPresent()) {
            expenseRepository.nullifyCategoryId(existing.get().id());
            expenseCategoryRepository.deleteById(existing.get().id());
        }
    }

    @Transactional
    public void deleteCategoryById(UUID categoryId) {
        var existing = expenseCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Expense category not found"));
        expenseRepository.nullifyCategoryId(categoryId);
        expenseCategoryRepository.deleteById(categoryId);
    }
}
