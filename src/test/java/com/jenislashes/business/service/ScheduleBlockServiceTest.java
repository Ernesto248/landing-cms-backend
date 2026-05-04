package com.jenislashes.business.service;

import com.jenislashes.business.dto.CreateScheduleBlockRequest;
import com.jenislashes.business.model.ScheduleBlockRecord;
import com.jenislashes.business.repository.ScheduleBlockRepository;
import com.jenislashes.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleBlockService")
class ScheduleBlockServiceTest {

    @Mock
    private ScheduleBlockRepository scheduleBlockRepository;

    @InjectMocks
    private ScheduleBlockService scheduleBlockService;

    @Test
    void createBlock_should_throw_when_partial_block_is_missing_times() {
        assertThrows(BadRequestException.class, () -> scheduleBlockService.createBlock(
                new CreateScheduleBlockRequest(LocalDate.of(2026, 5, 10), null, LocalTime.of(12, 0), "desc", false)
        ));
    }

    @Test
    void createBlock_should_store_full_day_block_with_null_times_and_trimmed_reason() {
        var response = scheduleBlockService.createBlock(
                new CreateScheduleBlockRequest(LocalDate.of(2026, 5, 10), LocalTime.of(9, 0), LocalTime.of(12, 0), "  Vacaciones  ", true)
        );

        ArgumentCaptor<ScheduleBlockRecord> recordCaptor = ArgumentCaptor.forClass(ScheduleBlockRecord.class);
        verify(scheduleBlockRepository).insert(recordCaptor.capture());

        ScheduleBlockRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals(LocalDate.of(2026, 5, 10), savedRecord.blockDate()),
                () -> assertNull(savedRecord.startTime()),
                () -> assertNull(savedRecord.endTime()),
                () -> assertEquals("Vacaciones", savedRecord.reason()),
                () -> assertEquals(true, savedRecord.isFullDay()),
                () -> assertEquals(true, response.isFullDay()),
                () -> assertNull(response.startTime()),
                () -> assertNull(response.endTime())
        );
    }

    @Test
    void createBlock_should_throw_when_start_time_is_not_before_end_time() {
        assertThrows(BadRequestException.class, () -> scheduleBlockService.createBlock(
                new CreateScheduleBlockRequest(LocalDate.of(2026, 5, 10), LocalTime.of(12, 0), LocalTime.of(12, 0), null, false)
        ));
    }
}
