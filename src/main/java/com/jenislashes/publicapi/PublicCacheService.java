package com.jenislashes.publicapi;

import com.jenislashes.business.dto.BusinessHourResponse;
import com.jenislashes.business.dto.BusinessProfileResponse;
import com.jenislashes.business.service.BusinessHoursService;
import com.jenislashes.business.service.BusinessProfileService;
import com.jenislashes.content.dto.LandingContentResponse;
import com.jenislashes.content.service.LandingContentService;
import com.jenislashes.content.testimonial.dto.TestimonialResponse;
import com.jenislashes.content.testimonial.service.TestimonialService;
import com.jenislashes.media.dto.GalleryItemResponse;
import com.jenislashes.media.service.GalleryService;
import com.jenislashes.servicecatalog.dto.ServiceResponse;
import com.jenislashes.servicecatalog.service.ServiceCatalogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class PublicCacheService {

    private final ServiceCatalogService serviceCatalogService;
    private final BusinessProfileService businessProfileService;
    private final BusinessHoursService businessHoursService;
    private final LandingContentService landingContentService;
    private final TestimonialService testimonialService;
    private final GalleryService galleryService;

    private volatile List<ServiceResponse> services;
    private volatile BusinessProfileResponse businessProfile;
    private volatile List<BusinessHourResponse> businessHours;
    private volatile List<LandingContentResponse> content;
    private volatile List<TestimonialResponse> testimonials;
    private volatile List<GalleryItemResponse> gallery;

    public PublicCacheService(
            ServiceCatalogService serviceCatalogService,
            BusinessProfileService businessProfileService,
            BusinessHoursService businessHoursService,
            LandingContentService landingContentService,
            TestimonialService testimonialService,
            GalleryService galleryService
    ) {
        this.serviceCatalogService = serviceCatalogService;
        this.businessProfileService = businessProfileService;
        this.businessHoursService = businessHoursService;
        this.landingContentService = landingContentService;
        this.testimonialService = testimonialService;
        this.galleryService = galleryService;
    }

    public List<ServiceResponse> services() {
        return cached(services, () -> services = List.copyOf(serviceCatalogService.getPublicServices()));
    }

    public BusinessProfileResponse businessProfile() {
        return cached(businessProfile, () -> businessProfile = businessProfileService.getProfile());
    }

    public List<BusinessHourResponse> businessHours() {
        return cached(businessHours, () -> businessHours = List.copyOf(businessHoursService.listHours()));
    }

    public List<LandingContentResponse> content() {
        return cached(content, () -> content = List.copyOf(landingContentService.listPublic()));
    }

    public List<TestimonialResponse> testimonials() {
        return cached(testimonials, () -> testimonials = List.copyOf(testimonialService.listPublic()));
    }

    public List<GalleryItemResponse> gallery() {
        return cached(gallery, () -> gallery = List.copyOf(galleryService.listPublic()));
    }

    public synchronized void evictAll() {
        services = null;
        businessProfile = null;
        businessHours = null;
        content = null;
        testimonials = null;
        gallery = null;
    }

    private synchronized <T> T cached(T current, Supplier<T> loader) {
        if (current != null) {
            return current;
        }

        return loader.get();
    }
}
