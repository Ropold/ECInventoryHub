package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.repository.LocationRepository;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
}