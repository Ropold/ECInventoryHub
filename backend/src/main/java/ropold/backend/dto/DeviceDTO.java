package ropold.backend.dto;

import ropold.backend.model.DeviceStatus;
import ropold.backend.model.DeviceType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DeviceDTO(
        UUID id,
        DeviceType type,
        String manufacturer,
        String modelName,
        String serialNumber,
        String inventoryNumber,
        LocalDate purchaseDate,
        DeviceStatus status,
        boolean defective,
        LocationDTO location,
        String notes,
        List<DeviceFileDTO> files
) {}