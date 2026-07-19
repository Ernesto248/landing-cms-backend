package com.jenislashes.content.controller;

import com.jenislashes.content.dto.LandingContentResponse;
import com.jenislashes.publicapi.PublicCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/content")
public class PublicLandingContentController {

    private final PublicCacheService publicCacheService;

    public PublicLandingContentController(PublicCacheService publicCacheService) {
        this.publicCacheService = publicCacheService;
    }

    @GetMapping
    public ResponseEntity<List<LandingContentResponse>> list() {
        return ResponseEntity.ok(publicCacheService.content());
    }
}
