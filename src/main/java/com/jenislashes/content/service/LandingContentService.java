package com.jenislashes.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jenislashes.content.dto.LandingContentResponse;
import com.jenislashes.content.dto.UpsertLandingContentRequest;
import com.jenislashes.content.model.LandingContentRecord;
import com.jenislashes.content.repository.LandingContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class LandingContentService {

    private final LandingContentRepository landingContentRepository;
    private final ObjectMapper objectMapper;

    public LandingContentService(LandingContentRepository landingContentRepository, ObjectMapper objectMapper) {
        this.landingContentRepository = landingContentRepository;
        this.objectMapper = objectMapper;
    }

    public List<LandingContentResponse> listAll() {
        return landingContentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<LandingContentResponse> listPublic() {
        return listAll();
    }

    @Transactional
    public LandingContentResponse upsert(UpsertLandingContentRequest request) {
        String contentKey = request.contentKey().trim();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String jsonValue = serializeJson(request.jsonValue());

        LandingContentRecord existing = landingContentRepository.findByContentKey(contentKey).orElse(null);
        if (existing == null) {
            LandingContentRecord created = new LandingContentRecord(
                    UUID.randomUUID(),
                    contentKey,
                    normalizeNullable(request.title()),
                    normalizeNullable(request.subtitle()),
                    normalizeNullable(request.body()),
                    jsonValue,
                    now
            );
            landingContentRepository.insert(created);
            return toResponse(created);
        }

        LandingContentRecord updated = new LandingContentRecord(
                existing.id(),
                existing.contentKey(),
                normalizeNullable(request.title()),
                normalizeNullable(request.subtitle()),
                normalizeNullable(request.body()),
                jsonValue,
                now
        );
        landingContentRepository.update(updated);
        return toResponse(updated);
    }

    private LandingContentResponse toResponse(LandingContentRecord record) {
        return new LandingContentResponse(
                record.id(),
                record.contentKey(),
                record.title(),
                record.subtitle(),
                record.body(),
                deserializeJson(record.jsonValue()),
                record.updatedAt()
        );
    }

    private String serializeJson(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid JSON value", exception);
        }
    }

    private JsonNode deserializeJson(String jsonValue) {
        if (jsonValue == null || jsonValue.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readTree(jsonValue);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored landing content JSON is invalid", exception);
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
