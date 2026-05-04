package com.jenislashes.content.controller;

import com.jenislashes.content.dto.LandingContentResponse;
import com.jenislashes.content.service.LandingContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/content")
public class PublicLandingContentController {

    private final LandingContentService landingContentService;

    public PublicLandingContentController(LandingContentService landingContentService) {
        this.landingContentService = landingContentService;
    }

    @GetMapping
    public ResponseEntity<List<LandingContentResponse>> list() {
        return ResponseEntity.ok(landingContentService.listPublic());
    }
}
