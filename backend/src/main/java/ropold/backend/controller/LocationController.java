package ropold.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import ropold.backend.dto.LocationDTO;
import ropold.backend.model.LocationModel;
import ropold.backend.service.LocationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

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
            @RequestBody LocationDTO locationDTO,
            @AuthenticationPrincipal OAuth2User authentication) {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return locationService.addLocation(new LocationModel(
                null,
                locationDTO.name(),
                locationDTO.address(),
                locationDTO.phone(),
                locationDTO.email(),
                locationDTO.notes()
        ));
    }

    @PutMapping("/{id}")
    public LocationModel updateLocation(
            @PathVariable UUID id,
            @RequestBody LocationDTO locationDTO,
            @AuthenticationPrincipal OAuth2User authentication) {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return locationService.updateLocation(new LocationModel(
                id,
                locationDTO.name(),
                locationDTO.address(),
                locationDTO.phone(),
                locationDTO.email(),
                locationDTO.notes()
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