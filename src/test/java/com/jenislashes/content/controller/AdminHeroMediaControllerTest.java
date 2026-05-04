package com.jenislashes.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jenislashes.content.dto.LandingContentResponse;
import com.jenislashes.content.service.LandingContentService;
import com.jenislashes.media.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminHeroMediaControllerTest {

    @Mock
    private LandingContentService landingContentService;

    @Mock
    private StorageService storageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminHeroMediaController(
                landingContentService,
                storageService,
                new ObjectMapper()
        )).build();
    }

    @Test
    void uploadHeroImage_should_store_public_url_inside_hero_content() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hero.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});
        ObjectNode jsonValue = new ObjectMapper().createObjectNode().put("heroBackgroundUrl", "https://cdn.example.com/hero.png");

        when(storageService.uploadHeroBackgroundImage(any())).thenReturn(
                new StorageService.StoredFile("branding/2026/05/hero.png", "https://cdn.example.com/hero.png")
        );
        when(landingContentService.upsert(any())).thenReturn(new LandingContentResponse(
                UUID.randomUUID(),
                "hero",
                "Nuevo titulo",
                "Nuevo eyebrow",
                "Nueva descripcion",
                jsonValue,
                OffsetDateTime.parse("2026-05-01T10:00:00Z")
        ));

        mockMvc.perform(multipart("/api/v1/admin/content/hero-image")
                        .file(file)
                        .param("heroEyebrow", "Nuevo eyebrow")
                        .param("heroTitle", "Nuevo titulo")
                        .param("heroDescription", "Nueva descripcion"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentKey").value("hero"))
                .andExpect(jsonPath("$.jsonValue.heroBackgroundUrl").value("https://cdn.example.com/hero.png"));
    }
}
