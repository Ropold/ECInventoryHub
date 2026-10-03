package ropold.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ropold.backend.exception.conflictexceptions.LocationHasDevicesException;
import ropold.backend.exception.notfoundexceptions.LocationNotFoundException;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.DeviceType;
import ropold.backend.model.LocationModel;
import ropold.backend.model.MoveDirection;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.LocationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocationServiceTest {

    LocationRepository locationRepository = mock(LocationRepository.class);
    DeviceRepository deviceRepository = mock(DeviceRepository.class);
    LocationService locationService = new LocationService(locationRepository, deviceRepository);

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
                null,
                null,
                "https://example.com/location1.jpg",
                1
        );

        LocationModel locationModel2 = new LocationModel(
                UUID.randomUUID(),
                "Location Two",
                "Beispielweg 2, 54321 Beispielstadt",
                "+49 170 7654321",
                "location.two@example.com",
                "Notes for location two",
                null,
                null,
                null,
                2
        );

        allLocations = List.of(locationModel1, locationModel2);
        when(locationRepository.findAllByOrderBySortOrderAscNameAsc()).thenReturn(allLocations);
        when(locationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
                null,
                null,
                null,
                0
        );

        LocationModel savedLocation = new LocationModel(
                UUID.randomUUID(),
                newLocation.getName(),
                newLocation.getAddress(),
                newLocation.getPhone(),
                newLocation.getEmail(),
                newLocation.getNotes(),
                null,
                null,
                newLocation.getImageUrl(),
                0
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
                null,
                null,
                existingLocation.getImageUrl(),
                0
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
        when(deviceRepository.findByLocationId(locationToDelete.getId())).thenReturn(List.of());

        locationService.deleteLocation(locationToDelete.getId());
        verify(locationRepository, times(1)).deleteById(locationToDelete.getId());
    }

    @Test
    void testDeleteLocation_WithBlockingDevices_ThrowsException() {
        LocationModel locationToDelete = allLocations.getFirst();
        when(locationRepository.findById(locationToDelete.getId())).thenReturn(Optional.of(locationToDelete));

        DeviceModel device = new DeviceModel();
        device.setId(UUID.randomUUID());
        device.setType(DeviceType.LAPTOP);
        device.setLocation(locationToDelete);

        when(deviceRepository.findByLocationId(locationToDelete.getId())).thenReturn(List.of(device));

        LocationHasDevicesException exception = assertThrows(
                LocationHasDevicesException.class,
                () -> locationService.deleteLocation(locationToDelete.getId())
        );

        assertEquals(1, exception.getDeviceDetails().size());
        assertEquals("Device ID: " + device.getId(), exception.getDeviceDetails().getFirst());
        verify(locationRepository, never()).deleteById(any());
    }

    @Test
    void testForceDeleteLocation_DetachesDevicesAndDeletesLocation() {
        LocationModel locationToDelete = allLocations.getFirst();
        when(locationRepository.findById(locationToDelete.getId())).thenReturn(Optional.of(locationToDelete));

        DeviceModel device = new DeviceModel();
        device.setId(UUID.randomUUID());
        device.setType(DeviceType.LAPTOP);
        device.setLocation(locationToDelete);

        List<DeviceModel> blockingDevices = List.of(device);
        when(deviceRepository.findByLocationId(locationToDelete.getId())).thenReturn(blockingDevices);

        locationService.forceDeleteLocation(locationToDelete.getId());

        assertNull(device.getLocation());
        verify(deviceRepository, times(1)).saveAll(blockingDevices);
        verify(locationRepository, times(1)).deleteById(locationToDelete.getId());
    }

    @Test
    void testForceDeleteLocation_NoDevices_StillDeletesLocation() {
        LocationModel locationToDelete = allLocations.getFirst();
        when(locationRepository.findById(locationToDelete.getId())).thenReturn(Optional.of(locationToDelete));
        when(deviceRepository.findByLocationId(locationToDelete.getId())).thenReturn(List.of());

        locationService.forceDeleteLocation(locationToDelete.getId());

        verify(deviceRepository, times(1)).saveAll(List.of());
        verify(locationRepository, times(1)).deleteById(locationToDelete.getId());
    }

    @Test
    void testForceDeleteLocation_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(locationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                LocationNotFoundException.class,
                () -> locationService.forceDeleteLocation(nonExistentId)
        );

        verify(locationRepository, never()).deleteById(any());
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

    @Test
    void testNextSortOrder_AppendsAfterHighest() {
        when(locationRepository.findTopByOrderBySortOrderDesc()).thenReturn(Optional.of(allLocations.getLast()));
        assertEquals(3, locationService.nextSortOrder());
    }

    @Test
    void testNextSortOrder_NoLocations_StartsAtOne() {
        when(locationRepository.findTopByOrderBySortOrderDesc()).thenReturn(Optional.empty());
        assertEquals(1, locationService.nextSortOrder());
    }

    @Test
    void testMoveLocation_Down_SwapsWithNextAndRenumbers() {
        LocationModel first = allLocations.getFirst();
        LocationModel second = allLocations.getLast();

        List<LocationModel> result = locationService.moveLocation(first.getId(), MoveDirection.DOWN);

        assertSame(second, result.get(0));
        assertSame(first, result.get(1));
        assertEquals(1, second.getSortOrder());
        assertEquals(2, first.getSortOrder());
        verify(locationRepository, times(1)).saveAll(any());
    }

    @Test
    void testMoveLocation_Up_SwapsWithPrevious() {
        LocationModel first = allLocations.getFirst();
        LocationModel second = allLocations.getLast();

        List<LocationModel> result = locationService.moveLocation(second.getId(), MoveDirection.UP);

        assertSame(second, result.get(0));
        assertSame(first, result.get(1));
    }

    @Test
    void testMoveLocation_FirstUp_KeepsOrder() {
        LocationModel first = allLocations.getFirst();

        List<LocationModel> result = locationService.moveLocation(first.getId(), MoveDirection.UP);

        assertSame(first, result.get(0));
        assertEquals(1, first.getSortOrder());
    }

    @Test
    void testMoveLocation_EqualSortOrders_RenumbersSequentially() {
        allLocations.forEach(location -> location.setSortOrder(0));

        locationService.moveLocation(allLocations.getFirst().getId(), MoveDirection.UP);

        assertEquals(1, allLocations.getFirst().getSortOrder());
        assertEquals(2, allLocations.getLast().getSortOrder());
    }

    @Test
    void testMoveLocation_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(
                LocationNotFoundException.class,
                () -> locationService.moveLocation(nonExistentId, MoveDirection.DOWN)
        );

        verify(locationRepository, never()).saveAll(any());
    }
}
