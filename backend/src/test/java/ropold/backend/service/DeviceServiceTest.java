package ropold.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ropold.backend.dto.DeviceDTO;
import ropold.backend.dto.LocationDTO;
import ropold.backend.exception.notfoundexceptions.DeviceNotFoundException;
import ropold.backend.exception.notfoundexceptions.LocationNotFoundException;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.DeviceStatus;
import ropold.backend.model.DeviceType;
import ropold.backend.model.LocationModel;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.LocationRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceServiceTest {

    DeviceRepository deviceRepository = mock(DeviceRepository.class);
    LocationRepository locationRepository = mock(LocationRepository.class);
    DeviceService deviceService = new DeviceService(deviceRepository, locationRepository);

    List<DeviceModel> allDevices;
    LocationModel locationModel1;

    private LocationDTO toLocationDTO(LocationModel location) {
        return new LocationDTO(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getPhone(),
                location.getEmail(),
                location.getNotes(),
                location.getImageUrl()
        );
    }

    @BeforeEach
    void setUp() {

        locationModel1 = new LocationModel(
                UUID.randomUUID(),
                "Location One",
                "Musterstrasse 1, 12345 Musterstadt",
                "+49 170 1234567",
                "location.one@example.com",
                "Notes for location one",
                "http://example.com/location1.jpg"
        );

        DeviceModel deviceModel1 = new DeviceModel(
                UUID.randomUUID(),
                DeviceType.LAPTOP,
                "Dell",
                "Latitude 5420",
                "SN-1001",
                "INV-1001",
                LocalDate.of(2023, 1, 15),
                DeviceStatus.ASSIGNED,
                false,
                locationModel1,
                "Notes for device one",
                new ArrayList<>()
        );

        DeviceModel deviceModel2 = new DeviceModel(
                UUID.randomUUID(),
                DeviceType.PHONE,
                "Apple",
                "iPhone 14",
                "SN-2002",
                "INV-2002",
                LocalDate.of(2023, 6, 1),
                DeviceStatus.AVAILABLE,
                false,
                null,
                "Notes for device two",
                new ArrayList<>()
        );

        allDevices = List.of(deviceModel1, deviceModel2);
        when(deviceRepository.findAll()).thenReturn(allDevices);
    }

    @Test
    void getAllDevices() {
        List<DeviceModel> devices = deviceService.findAllDevices();
        assertEquals(devices, allDevices);
    }

    @Test
    void testGetDeviceById() {
        DeviceModel deviceModel = allDevices.getFirst();
        when(deviceRepository.findById(deviceModel.getId())).thenReturn(Optional.of(deviceModel));
        DeviceModel result = deviceService.getDeviceById(deviceModel.getId());
        assertEquals(deviceModel, result);
    }

    @Test
    void testGetDeviceById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                DeviceNotFoundException.class,
                () -> deviceService.getDeviceById(nonExistentId)
        );
    }

    @Test
    void testAddDevice() {
        DeviceDTO deviceDTO = new DeviceDTO(
                null,
                DeviceType.MONITOR,
                "Samsung",
                "Odyssey G7",
                "SN-3003",
                "INV-3003",
                LocalDate.of(2024, 2, 1),
                DeviceStatus.AVAILABLE,
                false,
                toLocationDTO(locationModel1),
                "Notes for new device",
                null
        );

        when(locationRepository.findById(locationModel1.getId())).thenReturn(Optional.of(locationModel1));
        when(deviceRepository.save(any(DeviceModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeviceModel result = deviceService.addDevice(deviceDTO);

        assertEquals(deviceDTO.manufacturer(), result.getManufacturer());
        assertEquals(locationModel1, result.getLocation());
        verify(deviceRepository, times(1)).save(any(DeviceModel.class));
    }

    @Test
    void testAddDevice_WithoutLocation() {
        DeviceDTO deviceDTO = new DeviceDTO(
                null,
                DeviceType.ACCESSORY,
                "Logitech",
                "MX Master 3",
                "SN-4004",
                "INV-4004",
                LocalDate.of(2024, 3, 1),
                DeviceStatus.AVAILABLE,
                false,
                null,
                "Notes for accessory",
                null
        );

        when(deviceRepository.save(any(DeviceModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeviceModel result = deviceService.addDevice(deviceDTO);

        assertNull(result.getLocation());
        verify(locationRepository, never()).findById(any());
        verify(deviceRepository, times(1)).save(any(DeviceModel.class));
    }

    @Test
    void testAddDevice_LocationNotFound() {
        DeviceDTO deviceDTO = new DeviceDTO(
                null,
                DeviceType.MONITOR,
                "Samsung",
                "Odyssey G7",
                "SN-3003",
                "INV-3003",
                LocalDate.of(2024, 2, 1),
                DeviceStatus.AVAILABLE,
                false,
                toLocationDTO(locationModel1),
                "Notes for new device",
                null
        );

        when(locationRepository.findById(locationModel1.getId())).thenReturn(Optional.empty());

        assertThrows(
                LocationNotFoundException.class,
                () -> deviceService.addDevice(deviceDTO)
        );

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void testUpdateDevice() {
        DeviceModel existing = allDevices.getFirst();
        DeviceDTO deviceDTO = new DeviceDTO(
                existing.getId(),
                DeviceType.LAPTOP,
                "Dell",
                "Latitude 5430",
                existing.getSerialNumber(),
                existing.getInventoryNumber(),
                existing.getPurchaseDate(),
                DeviceStatus.IN_REPAIR,
                true,
                toLocationDTO(locationModel1),
                "Updated notes",
                null
        );

        when(deviceRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(locationRepository.findById(locationModel1.getId())).thenReturn(Optional.of(locationModel1));
        when(deviceRepository.save(existing)).thenReturn(existing);

        DeviceModel result = deviceService.updateDevice(existing.getId(), deviceDTO);

        assertEquals("Latitude 5430", result.getModelName());
        assertEquals(DeviceStatus.IN_REPAIR, result.getStatus());
        assertEquals("Updated notes", result.getNotes());
        verify(deviceRepository, times(1)).save(existing);
    }

    @Test
    void testUpdateDevice_DeviceNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        DeviceDTO deviceDTO = new DeviceDTO(
                nonExistentId, DeviceType.LAPTOP, "Dell", "Latitude 5430",
                "SN-9999", "INV-9999", LocalDate.now(), DeviceStatus.AVAILABLE,
                false, null, "Notes", null
        );

        when(deviceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                DeviceNotFoundException.class,
                () -> deviceService.updateDevice(nonExistentId, deviceDTO)
        );

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void testUpdateDevice_LocationNotFound() {
        DeviceModel existing = allDevices.getFirst();
        DeviceDTO deviceDTO = new DeviceDTO(
                existing.getId(),
                existing.getType(),
                existing.getManufacturer(),
                existing.getModelName(),
                existing.getSerialNumber(),
                existing.getInventoryNumber(),
                existing.getPurchaseDate(),
                existing.getStatus(),
                existing.isDefective(),
                toLocationDTO(locationModel1),
                existing.getNotes(),
                null
        );

        when(deviceRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(locationRepository.findById(locationModel1.getId())).thenReturn(Optional.empty());

        assertThrows(
                LocationNotFoundException.class,
                () -> deviceService.updateDevice(existing.getId(), deviceDTO)
        );

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void testDeleteDevice() {
        DeviceModel deviceToDelete = allDevices.getFirst();
        when(deviceRepository.findById(deviceToDelete.getId())).thenReturn(Optional.of(deviceToDelete));
        deviceService.deleteDevice(deviceToDelete.getId());
        verify(deviceRepository, times(1)).deleteById(deviceToDelete.getId());
    }

    @Test
    void testDeleteDevice_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                DeviceNotFoundException.class,
                () -> deviceService.deleteDevice(nonExistentId)
        );

        verify(deviceRepository, never()).deleteById(any());
    }
}