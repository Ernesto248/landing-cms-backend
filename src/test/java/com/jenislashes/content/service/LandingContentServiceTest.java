package com.jenislashes.content.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jenislashes.content.dto.UpsertLandingContentRequest;
import com.jenislashes.content.model.LandingContentRecord;
import com.jenislashes.content.repository.LandingContentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LandingContentService")
class LandingContentServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LandingContentRepository landingContentRepository;

    private LandingContentService landingContentService() {
        return new LandingContentService(landingContentRepository, objectMapper);
    }

    @Test
    void upsert_should_insert_new_content_with_trimmed_fields_and_serialized_json() {
        ObjectNode jsonValue = objectMapper.createObjectNode().put("headline", "Hola");
        when(landingContentRepository.findByContentKey("hero")).thenReturn(Optional.empty());

        var response = landingContentService().upsert(new UpsertLandingContentRequest("  hero  ", "  Bienvenida  ", "   ", null, jsonValue));

        ArgumentCaptor<LandingContentRecord> recordCaptor = ArgumentCaptor.forClass(LandingContentRecord.class);
        verify(landingContentRepository).insert(recordCaptor.capture());

        LandingContentRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals("hero", savedRecord.contentKey()),
                () -> assertEquals("Bienvenida", savedRecord.title()),
                () -> assertNull(savedRecord.subtitle()),
                () -> assertNull(savedRecord.body()),
                () -> assertEquals("{\"headline\":\"Hola\"}", savedRecord.jsonValue()),
                () -> assertEquals(savedRecord.contentKey(), response.contentKey()),
                () -> assertEquals("Bienvenida", response.title()),
                () -> assertEquals("Hola", response.jsonValue().get("headline").asText())
        );
    }

    @Test
    void upsert_should_update_existing_content_and_preserve_identity() {
        UUID contentId = UUID.randomUUID();
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-05-01T10:00:00Z");
        LandingContentRecord existing = new LandingContentRecord(
                contentId,
                "hero",
                "Titulo",
                null,
                null,
                "{\"headline\":\"Viejo\"}",
                updatedAt
        );

        when(landingContentRepository.findByContentKey("hero")).thenReturn(Optional.of(existing));

        var response = landingContentService().upsert(new UpsertLandingContentRequest(
                "hero",
                "  Nuevo titulo  ",
                "  Subtitulo  ",
                "  Texto largo  ",
                objectMapper.createObjectNode().put("headline", "Nuevo")
        ));

        ArgumentCaptor<LandingContentRecord> recordCaptor = ArgumentCaptor.forClass(LandingContentRecord.class);
        verify(landingContentRepository).update(recordCaptor.capture());

        LandingContentRecord updatedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals(contentId, updatedRecord.id()),
                () -> assertEquals("hero", updatedRecord.contentKey()),
                () -> assertEquals("Nuevo titulo", updatedRecord.title()),
                () -> assertEquals("Subtitulo", updatedRecord.subtitle()),
                () -> assertEquals("Texto largo", updatedRecord.body()),
                () -> assertEquals("Nuevo titulo", response.title()),
                () -> assertEquals("Nuevo", response.jsonValue().get("headline").asText())
        );
    }
}
