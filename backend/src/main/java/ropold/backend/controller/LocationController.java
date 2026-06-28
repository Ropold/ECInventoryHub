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
    public List<LocationDTO> getAllLocations() {
        return locationService.findAllLocations().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public LocationDTO getLocationById(@PathVariable UUID id) {
        return toDto(locationService.getLocationById(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public LocationDTO addLocation(
            @RequestBody LocationDTO locationDTO,
            @AuthenticationPrincipal OAuth2User authentication) {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return toDto(locationService.addLocation(toModel(null, locationDTO)));
    }

    @PutMapping("/{id}")
    public LocationDTO updateLocation(
            @PathVariable UUID id,
            @RequestBody LocationDTO locationDTO,
            @AuthenticationPrincipal OAuth2User authentication) {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return toDto(locationService.updateLocation(toModel(id, locationDTO)));
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

    private LocationDTO toDto(LocationModel model) {
        return new LocationDTO(
                model.getId(),
                model.getName(),
                model.getAddress(),
                model.getPhone(),
                model.getEmail(),
                model.getNotes()
        );
    }

    private LocationModel toModel(UUID id, LocationDTO dto) {
        return new LocationModel(
                id,
                dto.name(),
                dto.address(),
                dto.phone(),
                dto.email(),
                dto.notes()
        );
    }
}