package com.jenislashes.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jenislashes.content.dto.LandingContentResponse;
import com.jenislashes.content.dto.UpsertLandingContentRequest;
import com.jenislashes.content.service.LandingContentService;
import com.jenislashes.media.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/content")
public class AdminHeroMediaController {

    private final LandingContentService landingContentService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    public AdminHeroMediaController(
            LandingContentService landingContentService,
            StorageService storageService,
            ObjectMapper objectMapper
    ) {
        this.landingContentService = landingContentService;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(path = "/hero-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LandingContentResponse> uploadHeroImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String heroEyebrow,
            @RequestParam(required = false) String heroTitle,
            @RequestParam(required = false) String heroDescription
    ) {
        StorageService.StoredFile storedFile = storageService.uploadHeroBackgroundImage(file);
        ObjectNode jsonValue = objectMapper.createObjectNode();
        jsonValue.put("heroBackgroundUrl", storedFile.publicUrl());
        putIfNotBlank(jsonValue, "heroEyebrow", heroEyebrow);
        putIfNotBlank(jsonValue, "heroTitle", heroTitle);
        putIfNotBlank(jsonValue, "heroDescription", heroDescription);

        LandingContentResponse response = landingContentService.upsert(new UpsertLandingContentRequest(
                "hero",
                heroTitle,
                heroEyebrow,
                heroDescription,
                jsonValue
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void putIfNotBlank(ObjectNode jsonValue, String fieldName, String value) {
        if (value != null && !value.trim().isEmpty()) {
            jsonValue.put(fieldName, value.trim());
        }
    }
}
