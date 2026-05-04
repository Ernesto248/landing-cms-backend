package com.jenislashes.content.controller;

import com.jenislashes.content.dto.LandingContentResponse;
import com.jenislashes.content.dto.UpsertLandingContentRequest;
import com.jenislashes.content.service.LandingContentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/content")
public class AdminLandingContentController {

    private final LandingContentService landingContentService;

    public AdminLandingContentController(LandingContentService landingContentService) {
        this.landingContentService = landingContentService;
    }

    @GetMapping
    public ResponseEntity<List<LandingContentResponse>> list() {
        return ResponseEntity.ok(landingContentService.listAll());
    }

    @PutMapping
    public ResponseEntity<LandingContentResponse> upsert(@Valid @RequestBody UpsertLandingContentRequest request) {
        return ResponseEntity.ok(landingContentService.upsert(request));
    }
}
