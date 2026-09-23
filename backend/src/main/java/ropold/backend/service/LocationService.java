package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.exception.conflictexceptions.LocationHasDevicesException;
import ropold.backend.exception.notfoundexceptions.LocationNotFoundException;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.LocationModel;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.LocationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final DeviceRepository deviceRepository;

    public List<LocationModel> findAllLocations() {
        return locationRepository.findAll();
    }

    public LocationModel getLocationById(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + id));
    }

    public LocationModel addLocation(LocationModel location) {
        return locationRepository.save(location);
    }

    public LocationModel updateLocation(LocationModel location) {
        if (!locationRepository.existsById(location.getId())) {
            throw new LocationNotFoundException("Location not found with id: " + location.getId());
        }
        return locationRepository.save(location);
    }

    public void deleteLocation(UUID id) {
        locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + id));

        List<DeviceModel> blockingDevices = deviceRepository.findByLocationId(id);
        if (!blockingDevices.isEmpty()) {
            throw new LocationHasDevicesException(
                    "Location cannot be deleted because there are still devices referencing it.",
                    blockingDevices.stream().map(LocationService::describeDevice).toList());
        }

        locationRepository.deleteById(id);
    }

    public void forceDeleteLocation(UUID id) {
        locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + id));

        List<DeviceModel> devicesToDetach = deviceRepository.findByLocationId(id);
        devicesToDetach.forEach(device -> device.setLocation(null));
        deviceRepository.saveAll(devicesToDetach);

        locationRepository.deleteById(id);
    }

    private static String describeDevice(DeviceModel device) {
        return "Device ID: " + device.getId();
    }
}