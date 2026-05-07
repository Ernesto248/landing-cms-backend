package com.jenislashes.finance.controller;

import com.jenislashes.finance.dto.CreateExpenseCategoryRequest;
import com.jenislashes.finance.dto.ExpenseCategoryResponse;
import com.jenislashes.finance.service.ExpenseCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/expense-categories")
public class AdminExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    public AdminExpenseCategoryController(ExpenseCategoryService expenseCategoryService) {
        this.expenseCategoryService = expenseCategoryService;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseCategoryResponse>> list() {
        return ResponseEntity.ok(expenseCategoryService.listCategories());
    }

    @PostMapping
    public ResponseEntity<ExpenseCategoryResponse> create(@Valid @RequestBody CreateExpenseCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseCategoryService.createCategory(request));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable UUID categoryId) {
        expenseCategoryService.deleteCategoryById(categoryId);
        return ResponseEntity.noContent().build();
    }
}
