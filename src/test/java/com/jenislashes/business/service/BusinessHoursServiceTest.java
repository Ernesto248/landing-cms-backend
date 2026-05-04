package com.jenislashes.business.service;

import com.jenislashes.business.dto.UpsertBusinessHourRequest;
import com.jenislashes.business.model.BusinessHourRecord;
import com.jenislashes.business.repository.BusinessHoursRepository;
import com.jenislashes.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessHoursService")
class BusinessHoursServiceTest {

    @Mock
    private BusinessHoursRepository businessHoursRepository;

    @InjectMocks
    private BusinessHoursService businessHoursService;

    @Test
    void upsertHours_should_throw_when_open_day_is_missing_times() {
        assertThrows(BadRequestException.class, () -> businessHoursService.upsertHours(
                List.of(new UpsertBusinessHourRequest((short) 1, null, LocalTime.of(17, 0), false))
        ));
    }

    @Test
    void upsertHours_should_insert_closed_day_with_null_times() {
        when(businessHoursRepository.findByDayOfWeek((short) 7)).thenReturn(Optional.empty());
        when(businessHoursRepository.findAll()).thenReturn(List.of(
                new BusinessHourRecord(UUID.randomUUID(), (short) 7, null, null, true)
        ));

        var response = businessHoursService.upsertHours(List.of(
                new UpsertBusinessHourRequest((short) 7, LocalTime.of(9, 0), LocalTime.of(17, 0), true)
        ));

        ArgumentCaptor<BusinessHourRecord> recordCaptor = ArgumentCaptor.forClass(BusinessHourRecord.class);
        verify(businessHoursRepository).insert(recordCaptor.capture());

        BusinessHourRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals((short) 7, savedRecord.dayOfWeek()),
                () -> assertNull(savedRecord.openTime()),
                () -> assertNull(savedRecord.closeTime()),
                () -> assertEquals(true, savedRecord.isClosed()),
                () -> assertEquals(1, response.size()),
                () -> assertEquals(true, response.getFirst().isClosed())
        );
    }

    @Test
    void upsertHours_should_update_existing_day_when_record_exists() {
        UUID hourId = UUID.randomUUID();
        BusinessHourRecord existing = new BusinessHourRecord(hourId, (short) 1, LocalTime.of(9, 0), LocalTime.of(17, 0), false);
        when(businessHoursRepository.findByDayOfWeek((short) 1)).thenReturn(Optional.of(existing));
        when(businessHoursRepository.findAll()).thenReturn(List.of(
                new BusinessHourRecord(hourId, (short) 1, LocalTime.of(10, 0), LocalTime.of(18, 0), false)
        ));

        businessHoursService.upsertHours(List.of(
                new UpsertBusinessHourRequest((short) 1, LocalTime.of(10, 0), LocalTime.of(18, 0), false)
        ));

        ArgumentCaptor<BusinessHourRecord> recordCaptor = ArgumentCaptor.forClass(BusinessHourRecord.class);
        verify(businessHoursRepository).update(recordCaptor.capture());

        BusinessHourRecord updatedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals(hourId, updatedRecord.id()),
                () -> assertEquals(LocalTime.of(10, 0), updatedRecord.openTime()),
                () -> assertEquals(LocalTime.of(18, 0), updatedRecord.closeTime()),
                () -> assertEquals(false, updatedRecord.isClosed())
        );
    }
}
