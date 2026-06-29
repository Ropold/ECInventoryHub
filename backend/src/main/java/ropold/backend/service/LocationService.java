package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.exception.notfoundexceptions.LocationNotFoundException;
import ropold.backend.model.LocationModel;
import ropold.backend.repository.LocationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

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
        locationRepository.deleteById(id);
    }
}