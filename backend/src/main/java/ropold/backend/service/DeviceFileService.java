package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.repository.DeviceFileRepository;

@Service
@RequiredArgsConstructor
public class DeviceFileService {
    private final DeviceFileRepository deviceFileRepository;
}