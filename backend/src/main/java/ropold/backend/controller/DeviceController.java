package ropold.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ropold.backend.service.DeviceService;
import ropold.backend.service.ImageUploadUtil;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
    private final ImageUploadUtil imageUploadUtil;
}