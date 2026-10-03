package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.exception.conflictexceptions.LocationHasDevicesException;
import ropold.backend.exception.notfoundexceptions.LocationNotFoundException;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.LocationModel;
import ropold.backend.model.MoveDirection;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.LocationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final DeviceRepository deviceRepository;

    public List<LocationModel> findAllLocations() {
        return locationRepository.findAllByOrderBySortOrderAscNameAsc();
    }

    // Neue Standorte landen am Ende der Liste
    public int nextSortOrder() {
        return locationRepository.findTopByOrderBySortOrderDesc()
                .map(location -> location.getSortOrder() + 1)
                .orElse(1);
    }

    // Tauscht den Standort mit seinem Nachbarn und nummeriert danach alle fortlaufend (1, 2, 3 …),
    // damit auch Altdaten mit gleichem sort_order sauber sortiert werden. Liefert die neue Reihenfolge.
    public List<LocationModel> moveLocation(UUID id, MoveDirection direction) {
        List<LocationModel> ordered = new ArrayList<>(findAllLocations());
        int index = IntStream.range(0, ordered.size())
                .filter(i -> ordered.get(i).getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + id));

        int neighbor = direction == MoveDirection.UP ? index - 1 : index + 1;
        if (neighbor >= 0 && neighbor < ordered.size()) {
            Collections.swap(ordered, index, neighbor);
        }

        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).setSortOrder(i + 1);
        }
        return locationRepository.saveAll(ordered);
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