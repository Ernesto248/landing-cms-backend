package com.jenislashes.publicapi;

import com.jenislashes.business.service.BusinessHoursService;
import com.jenislashes.business.service.BusinessProfileService;
import com.jenislashes.content.service.LandingContentService;
import com.jenislashes.content.testimonial.service.TestimonialService;
import com.jenislashes.media.service.GalleryService;
import com.jenislashes.servicecatalog.service.ServiceCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCacheServiceTest {

    @Mock
    private ServiceCatalogService serviceCatalogService;

    @Mock
    private BusinessProfileService businessProfileService;

    @Mock
    private BusinessHoursService businessHoursService;

    @Mock
    private LandingContentService landingContentService;

    @Mock
    private TestimonialService testimonialService;

    @Mock
    private GalleryService galleryService;

    private PublicCacheService publicCacheService;

    @BeforeEach
    void setUp() {
        publicCacheService = new PublicCacheService(
                serviceCatalogService,
                businessProfileService,
                businessHoursService,
                landingContentService,
                testimonialService,
                galleryService
        );
    }

    @Test
    void services_should_cache_until_evicted() {
        when(serviceCatalogService.getPublicServices()).thenReturn(List.of());

        publicCacheService.services();
        publicCacheService.services();
        publicCacheService.evictAll();
        publicCacheService.services();

        verify(serviceCatalogService, times(2)).getPublicServices();
    }
}
