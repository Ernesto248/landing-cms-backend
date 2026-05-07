package com.jenislashes.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jenislashes.finance.service.ExpenseCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminExpenseCategoryControllerTest {

    @Mock
    private ExpenseCategoryService expenseCategoryService;

    private MockMvc mockMvc;

    private static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminExpenseCategoryController(expenseCategoryService))
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper()))
                .build();
    }

    @Test
    void delete_should_return_no_content() throws Exception {
        UUID categoryId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(delete("/api/v1/admin/expense-categories/" + categoryId))
                .andExpect(status().isNoContent());

        verify(expenseCategoryService).deleteCategoryById(categoryId);
    }
}
