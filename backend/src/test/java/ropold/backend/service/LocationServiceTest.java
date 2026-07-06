package ropold.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ropold.backend.exception.notfoundexceptions.LocationNotFoundException;
import ropold.backend.model.LocationModel;
import ropold.backend.repository.LocationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocationServiceTest {

    LocationRepository locationRepository = mock(LocationRepository.class);
    LocationService locationService = new LocationService(locationRepository);

    List<LocationModel> allLocations;

    @BeforeEach
    void setUp() {

        LocationModel locationModel1 = new LocationModel(
                UUID.randomUUID(),
                "Location One",
                "Musterstrasse 1, 12345 Musterstadt",
                "+49 170 1234567",
                "location.one@example.com",
                "Notes for location one",
                "https://example.com/location1.jpg"
        );

        LocationModel locationModel2 = new LocationModel(
                UUID.randomUUID(),
                "Location Two",
                "Beispielweg 2, 54321 Beispielstadt",
                "+49 170 7654321",
                "location.two@example.com",
                "Notes for location two",
                null
        );

        allLocations = List.of(locationModel1, locationModel2);
        when(locationRepository.findAll()).thenReturn(allLocations);
    }

    @Test
    void getAllLocations() {
        List<LocationModel> locations = locationService.findAllLocations();
        assertEquals(locations, allLocations);
    }

    @Test
    void testGetLocationById() {
        LocationModel locationModel = allLocations.getFirst();
        when(locationRepository.findById(locationModel.getId())).thenReturn(Optional.of(locationModel));
        LocationModel result = locationService.getLocationById(locationModel.getId());
        assertEquals(locationModel, result);
    }

    @Test
    void testAddLocation() {
        LocationModel newLocation = new LocationModel(
                null,
                "New Location",
                "Neue Strasse 3, 11111 Neustadt",
                "+49 170 1112223",
                "new.location@example.com",
                "None",
                null
        );

        LocationModel savedLocation = new LocationModel(
                UUID.randomUUID(),
                newLocation.getName(),
                newLocation.getAddress(),
                newLocation.getPhone(),
                newLocation.getEmail(),
                newLocation.getNotes(),
                newLocation.getImageUrl()
        );

        when(locationRepository.save(newLocation)).thenReturn(savedLocation);
        LocationModel result = locationService.addLocation(newLocation);
        assertEquals(savedLocation, result);
    }

    @Test
    void testUpdateLocation() {
        LocationModel existingLocation = allLocations.getFirst();
        LocationModel updatedLocation = new LocationModel(
                existingLocation.getId(),
                "Updated Location Name",
                existingLocation.getAddress(),
                existingLocation.getPhone(),
                existingLocation.getEmail(),
                existingLocation.getNotes(),
                existingLocation.getImageUrl()
        );

        when(locationRepository.existsById(updatedLocation.getId())).thenReturn(true);
        when(locationRepository.save(updatedLocation)).thenReturn(updatedLocation);

        LocationModel result = locationService.updateLocation(updatedLocation);
        assertEquals(updatedLocation, result);
        verify(locationRepository, times(1)).save(updatedLocation);
    }

    @Test
    void testDeleteLocation() {
        LocationModel locationToDelete = allLocations.getFirst();
        when(locationRepository.findById(locationToDelete.getId())).thenReturn(Optional.of(locationToDelete));
        locationService.deleteLocation(locationToDelete.getId());
        verify(locationRepository, times(1)).deleteById(locationToDelete.getId());
    }

    @Test
    void testGetLocationById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(locationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                LocationNotFoundException.class,
                () -> locationService.getLocationById(nonExistentId)
        );
    }

    @Test
    void testUpdateLocation_NotFound() {
        LocationModel nonExistentLocation = allLocations.getFirst();
        when(locationRepository.existsById(nonExistentLocation.getId())).thenReturn(false);

        assertThrows(
                LocationNotFoundException.class,
                () -> locationService.updateLocation(nonExistentLocation)
        );

        verify(locationRepository, never()).save(any());
    }

    @Test
    void testDeleteLocation_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(locationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                LocationNotFoundException.class,
                () -> locationService.deleteLocation(nonExistentId)
        );

        verify(locationRepository, never()).deleteById(any());
    }
}