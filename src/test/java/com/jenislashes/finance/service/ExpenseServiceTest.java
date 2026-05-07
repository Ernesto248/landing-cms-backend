package com.jenislashes.finance.service;

import com.jenislashes.common.exception.BadRequestException;
import com.jenislashes.finance.dto.CreateExpenseRequest;
import com.jenislashes.finance.model.ExpenseCategoryRecord;
import com.jenislashes.finance.model.ExpenseRecord;
import com.jenislashes.finance.repository.ExpenseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseCategoryService expenseCategoryService;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void createExpense_should_throw_when_category_is_inactive() {
        UUID categoryId = UUID.randomUUID();
        when(expenseCategoryService.requireCategory(categoryId)).thenReturn(
                new ExpenseCategoryRecord(categoryId, "Transporte", false, OffsetDateTime.parse("2026-05-01T10:00:00Z"))
        );

        assertThrows(BadRequestException.class, () -> expenseService.createExpense(new CreateExpenseRequest(
                categoryId,
                LocalDate.of(2026, 5, 10),
                "Taxi",
                new BigDecimal("300.00"),
                null
        )));
    }

    @Test
    void createExpense_should_store_category_snapshot_and_trimmed_fields() {
        UUID categoryId = UUID.randomUUID();
        when(expenseCategoryService.requireCategory(categoryId)).thenReturn(
                new ExpenseCategoryRecord(categoryId, "Transporte", true, OffsetDateTime.parse("2026-05-01T10:00:00Z"))
        );

        var response = expenseService.createExpense(new CreateExpenseRequest(
                categoryId,
                LocalDate.of(2026, 5, 10),
                "  Taxi ida y vuelta  ",
                new BigDecimal("300.00"),
                "   "
        ));

        ArgumentCaptor<ExpenseRecord> recordCaptor = ArgumentCaptor.forClass(ExpenseRecord.class);
        verify(expenseRepository).insert(recordCaptor.capture());

        ExpenseRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals(categoryId, savedRecord.expenseCategoryId()),
                () -> assertEquals("Transporte", savedRecord.expenseCategoryName()),
                () -> assertEquals("Taxi ida y vuelta", savedRecord.description()),
                () -> assertNull(savedRecord.notes()),
                () -> assertEquals(new BigDecimal("300.00"), savedRecord.amount()),
                () -> assertEquals("Transporte", response.expenseCategoryName()),
                () -> assertEquals("Taxi ida y vuelta", response.description())
        );
    }

    @Test
    void createExpense_should_allow_expense_without_category() {
        var response = expenseService.createExpense(new CreateExpenseRequest(
                null,
                LocalDate.of(2026, 5, 11),
                "  Compra menor  ",
                new BigDecimal("150.00"),
                "  caja chica  "
        ));

        ArgumentCaptor<ExpenseRecord> recordCaptor = ArgumentCaptor.forClass(ExpenseRecord.class);
        verify(expenseRepository).insert(recordCaptor.capture());

        ExpenseRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertNull(savedRecord.expenseCategoryId()),
                () -> assertNull(savedRecord.expenseCategoryName()),
                () -> assertEquals("Compra menor", savedRecord.description()),
                () -> assertEquals("caja chica", savedRecord.notes()),
                () -> assertNull(response.expenseCategoryId())
        );
    }

    @Test
    void deleteExpense_should_call_repository_delete() {
        UUID expenseId = UUID.randomUUID();

        expenseService.deleteExpense(expenseId);

        verify(expenseRepository).deleteById(expenseId);
    }
}
