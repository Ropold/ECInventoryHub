package ropold.backend.dto;

import org.junit.jupiter.api.Test;
import ropold.backend.model.DeviceStatus;
import ropold.backend.model.DeviceType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceDTOTest {

    @Test
    void testDeviceDTOCreation() {
        UUID id = UUID.randomUUID();
        DeviceType type = DeviceType.LAPTOP;
        String manufacturer = "Dell";
        String modelName = "Latitude 5420";
        String serialNumber = "SN-1001";
        String inventoryNumber = "INV-1001";
        LocalDate purchaseDate = LocalDate.of(2023, 1, 15);
        DeviceStatus status = DeviceStatus.ASSIGNED;
        boolean defective = true;
        LocationDTO location = new LocationDTO(
                UUID.randomUUID(), "Location One", "Address", "Phone", "Email", "Notes", "ImageUrl"
        );
        String notes = "Notes for device one";
        List<DeviceFileDTO> files = List.of(
                new DeviceFileDTO(UUID.randomUUID(), "https://example.com/file.pdf", "application/pdf", null)
        );

        DeviceDTO deviceDTO = new DeviceDTO(
                id, type, manufacturer, modelName, serialNumber, inventoryNumber,
                purchaseDate, status, defective, location, notes, files
        );

        assertEquals(id, deviceDTO.id());
        assertEquals(type, deviceDTO.type());
        assertEquals(manufacturer, deviceDTO.manufacturer());
        assertEquals(modelName, deviceDTO.modelName());
        assertEquals(serialNumber, deviceDTO.serialNumber());
        assertEquals(inventoryNumber, deviceDTO.inventoryNumber());
        assertEquals(purchaseDate, deviceDTO.purchaseDate());
        assertEquals(status, deviceDTO.status());
        assertTrue(deviceDTO.defective());
        assertEquals(location, deviceDTO.location());
        assertEquals(notes, deviceDTO.notes());
        assertEquals(files, deviceDTO.files());
    }
}