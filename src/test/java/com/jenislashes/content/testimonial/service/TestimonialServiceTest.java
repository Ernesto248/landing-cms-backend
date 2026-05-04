package com.jenislashes.content.testimonial.service;

import com.jenislashes.common.exception.NotFoundException;
import com.jenislashes.content.testimonial.dto.UpsertTestimonialRequest;
import com.jenislashes.content.testimonial.model.TestimonialRecord;
import com.jenislashes.content.testimonial.repository.TestimonialRepository;
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
@DisplayName("TestimonialService")
class TestimonialServiceTest {

    @Mock
    private TestimonialRepository testimonialRepository;

    @InjectMocks
    private TestimonialService testimonialService;

    @Test
    void create_should_trim_text_fields_before_persisting() {
        var response = testimonialService.create(new UpsertTestimonialRequest("  Maria  ", "  Excelente servicio  ", (short) 5, true, 2));

        ArgumentCaptor<TestimonialRecord> recordCaptor = ArgumentCaptor.forClass(TestimonialRecord.class);
        verify(testimonialRepository).insert(recordCaptor.capture());

        TestimonialRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals("Maria", savedRecord.clientName()),
                () -> assertEquals("Excelente servicio", savedRecord.text()),
                () -> assertEquals((short) 5, savedRecord.rating()),
                () -> assertEquals(true, savedRecord.isFeatured()),
                () -> assertEquals(2, savedRecord.sortOrder()),
                () -> assertEquals("Maria", response.clientName()),
                () -> assertEquals("Excelente servicio", response.text())
        );
    }

    @Test
    void update_should_throw_when_testimonial_does_not_exist() {
        UUID testimonialId = UUID.randomUUID();
        when(testimonialRepository.findById(testimonialId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> testimonialService.update(
                testimonialId,
                new UpsertTestimonialRequest("Maria", "Excelente", (short) 5, true, 0)
        ));
    }

    @Test
    void update_should_preserve_created_at_and_apply_trimmed_values() {
        UUID testimonialId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-05-01T10:00:00Z");
        TestimonialRecord existing = new TestimonialRecord(testimonialId, "Viejo", "Viejo texto", (short) 4, false, 1, createdAt);
        when(testimonialRepository.findById(testimonialId)).thenReturn(Optional.of(existing));

        var response = testimonialService.update(
                testimonialId,
                new UpsertTestimonialRequest("  Ana  ", "  Muy buen resultado  ", (short) 5, true, 3)
        );

        ArgumentCaptor<TestimonialRecord> recordCaptor = ArgumentCaptor.forClass(TestimonialRecord.class);
        verify(testimonialRepository).update(recordCaptor.capture());

        TestimonialRecord updatedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals(testimonialId, updatedRecord.id()),
                () -> assertEquals(createdAt, updatedRecord.createdAt()),
                () -> assertEquals("Ana", updatedRecord.clientName()),
                () -> assertEquals("Muy buen resultado", updatedRecord.text()),
                () -> assertEquals((short) 5, updatedRecord.rating()),
                () -> assertEquals(true, updatedRecord.isFeatured()),
                () -> assertEquals(3, updatedRecord.sortOrder()),
                () -> assertEquals("Ana", response.clientName())
        );
    }
}
