package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.exception.notfoundexceptions.DeviceNotFoundException;
import ropold.backend.model.DeviceModel;
import ropold.backend.repository.DeviceRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public List<DeviceModel> findAllDevices() {
        return deviceRepository.findAll();
    }

    public DeviceModel getDeviceById(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
    }

    public void deleteDevice(UUID id) {
        deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
        deviceRepository.deleteById(id);
    }
}