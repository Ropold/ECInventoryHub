package ropold.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ropold.backend.dto.DeviceDTO;
import ropold.backend.dto.DeviceFileDTO;
import ropold.backend.model.DeviceFileModel;
import ropold.backend.model.DeviceModel;
import ropold.backend.service.CloudinaryService;
import ropold.backend.service.DeviceFileService;
import ropold.backend.service.DeviceService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
    private final DeviceFileService deviceFileService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public List<DeviceModel> getAllDevices() {
        return deviceService.findAllDevices();
    }

    @GetMapping("/{id}")
    public DeviceModel getDeviceById(@PathVariable UUID id) {
        return deviceService.getDeviceById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceModel addDevice(
            @RequestPart("deviceDTO") DeviceDTO deviceDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal OAuth2User authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        DeviceModel saved = deviceService.addDevice(deviceDTO);

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = uploadFile(file);
                    deviceFileService.saveFile(new DeviceFileModel(
                            null, saved, fileUrl, file.getContentType(), LocalDateTime.now()
                    ));
                }
            }
        }

        return deviceService.getDeviceById(saved.getId());
    }

    @PutMapping("/{id}")
    public DeviceModel updateDevice(
            @PathVariable UUID id,
            @RequestPart("deviceDTO") DeviceDTO deviceDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal OAuth2User authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        Set<UUID> keepIds = deviceDTO.files() != null
                ? deviceDTO.files().stream().map(DeviceFileDTO::id).collect(Collectors.toSet())
                : Collections.emptySet();

        DeviceModel existing = deviceService.getDeviceById(id);
        for (DeviceFileModel existingFile : existing.getFiles()) {
            if (!keepIds.contains(existingFile.getId())) {
                deleteFileFromCloudinary(existingFile);
            }
        }

        DeviceModel updated = deviceService.updateDevice(id, deviceDTO);

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = uploadFile(file);
                    deviceFileService.saveFile(new DeviceFileModel(
                            null, updated, fileUrl, file.getContentType(), LocalDateTime.now()
                    ));
                }
            }
        }

        return deviceService.getDeviceById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDevice(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User authentication) {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        deviceService.deleteDevice(id);
    }

    private String uploadFile(MultipartFile file) throws IOException {
        if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
            return cloudinaryService.uploadImage(file);
        }
        return cloudinaryService.uploadFile(file);
    }

    private void deleteFileFromCloudinary(DeviceFileModel file) {
        if (file.getFileType() != null && file.getFileType().startsWith("image/")) {
            cloudinaryService.deleteImage(file.getFileUrl());
        } else {
            cloudinaryService.deleteFile(file.getFileUrl());
        }
    }
}