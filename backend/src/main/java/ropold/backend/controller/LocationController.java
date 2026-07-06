package ropold.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ropold.backend.dto.LocationDTO;
import ropold.backend.model.LocationModel;
import ropold.backend.service.CloudinaryService;
import ropold.backend.service.ImageUploadUtil;
import ropold.backend.service.LocationService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final CloudinaryService cloudinaryService;
    private final ImageUploadUtil imageUploadUtil;

    @GetMapping
    public List<LocationModel> getAllLocations() {
        return locationService.findAllLocations();
    }

    @GetMapping("/{id}")
    public LocationModel getLocationById(@PathVariable UUID id) {
        return locationService.getLocationById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public LocationModel addLocation(
            @RequestPart("locationDTO") LocationDTO locationDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal OAuth2User authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(image);
        }

        return locationService.addLocation(new LocationModel(
                null,
                locationDTO.name(),
                locationDTO.address(),
                locationDTO.phone(),
                locationDTO.email(),
                locationDTO.notes(),
                imageUrl
        ));
    }

    @PutMapping("/{id}")
    public LocationModel updateLocation(
            @PathVariable UUID id,
            @RequestPart("locationDTO") LocationDTO locationDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal OAuth2User authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        LocationModel existing = locationService.getLocationById(id);
        String newImageUrl = imageUploadUtil.determineImageUrl(image, locationDTO.imageUrl(), existing.getImageUrl());
        imageUploadUtil.cleanupOldImageIfNeeded(existing.getImageUrl(), newImageUrl);

        return locationService.updateLocation(new LocationModel(
                existing.getId(),
                locationDTO.name(),
                locationDTO.address(),
                locationDTO.phone(),
                locationDTO.email(),
                locationDTO.notes(),
                newImageUrl
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User authentication) {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        locationService.deleteLocation(id);
    }
}
