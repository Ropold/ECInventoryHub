package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.dto.DeviceDTO;
import ropold.backend.dto.DeviceFileDTO;
import ropold.backend.exception.notfoundexceptions.DeviceNotFoundException;
import ropold.backend.exception.notfoundexceptions.LocationNotFoundException;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.LocationModel;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.LocationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final LocationRepository locationRepository;

    public List<DeviceModel> findAllDevices() {
        return deviceRepository.findAll();
    }

    public DeviceModel getDeviceById(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
    }

    public DeviceModel addDevice(DeviceDTO dto) {
        LocationModel location = resolveLocation(dto);

        return deviceRepository.save(new DeviceModel(
                null,
                dto.type(),
                dto.manufacturer(),
                dto.modelName(),
                dto.serialNumber(),
                dto.inventoryNumber(),
                dto.purchaseDate(),
                dto.status(),
                dto.defective(),
                location,
                dto.notes(),
                new ArrayList<>()
        ));
    }

    public DeviceModel updateDevice(UUID id, DeviceDTO dto) {
        DeviceModel existing = getDeviceById(id);
        LocationModel location = resolveLocation(dto);

        existing.setType(dto.type());
        existing.setManufacturer(dto.manufacturer());
        existing.setModelName(dto.modelName());
        existing.setSerialNumber(dto.serialNumber());
        existing.setInventoryNumber(dto.inventoryNumber());
        existing.setPurchaseDate(dto.purchaseDate());
        existing.setStatus(dto.status());
        existing.setDefective(dto.defective());
        existing.setLocation(location);
        existing.setNotes(dto.notes());

        Set<UUID> keepIds = dto.files() != null
                ? dto.files().stream().map(DeviceFileDTO::id).collect(Collectors.toSet())
                : Collections.emptySet();
        existing.getFiles().removeIf(f -> !keepIds.contains(f.getId()));

        return deviceRepository.save(existing);
    }

    public void deleteDevice(UUID id) {
        deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
        deviceRepository.deleteById(id);
    }

    private LocationModel resolveLocation(DeviceDTO dto) {
        if (dto.location() == null || dto.location().id() == null) {
            return null;
        }
        return locationRepository.findById(dto.location().id())
                .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + dto.location().id()));
    }
}