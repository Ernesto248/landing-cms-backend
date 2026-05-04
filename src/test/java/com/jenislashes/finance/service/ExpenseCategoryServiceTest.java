package com.jenislashes.finance.service;

import com.jenislashes.common.exception.NotFoundException;
import com.jenislashes.finance.dto.CreateExpenseCategoryRequest;
import com.jenislashes.finance.model.ExpenseCategoryRecord;
import com.jenislashes.finance.repository.ExpenseCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseCategoryService")
class ExpenseCategoryServiceTest {

    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;

    @InjectMocks
    private ExpenseCategoryService expenseCategoryService;

    @Test
    void requireCategory_should_throw_when_category_does_not_exist() {
        UUID categoryId = UUID.randomUUID();
        when(expenseCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseCategoryService.requireCategory(categoryId));
    }

    @Test
    void createCategory_should_trim_name_before_persisting() {
        var response = expenseCategoryService.createCategory(new CreateExpenseCategoryRequest("  Transporte  ", true));

        ArgumentCaptor<ExpenseCategoryRecord> recordCaptor = ArgumentCaptor.forClass(ExpenseCategoryRecord.class);
        verify(expenseCategoryRepository).insert(recordCaptor.capture());

        ExpenseCategoryRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals("Transporte", savedRecord.name()),
                () -> assertEquals(true, savedRecord.isActive()),
                () -> assertEquals(savedRecord.id(), response.id()),
                () -> assertEquals("Transporte", response.name()),
                () -> assertEquals(true, response.isActive())
        );
    }
}
