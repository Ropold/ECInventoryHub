package ropold.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ropold.backend.dto.EmployeeDTO;
import ropold.backend.model.EmployeeModel;
import ropold.backend.service.CloudinaryService;
import ropold.backend.service.EmployeeService;
import ropold.backend.service.ImageUploadUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final CloudinaryService cloudinaryService;
    private final ImageUploadUtil imageUploadUtil;

    @GetMapping
    public List<EmployeeModel> getAllEmployees() {
        return employeeService.findAllEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeModel getEmployeeById(@PathVariable UUID id) {
        return employeeService.getEmployeeById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public EmployeeModel addEmployee(
            @RequestPart("employeeDTO") EmployeeDTO employeeDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal OAuth2User authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(image);
        }

        return employeeService.addEmployee(new EmployeeModel(
                null,
                employeeDTO.personnelNumber(),
                employeeDTO.name(),
                employeeDTO.email(),
                employeeDTO.phone(),
                employeeDTO.address(),
                employeeDTO.department(),
                employeeDTO.active(),
                employeeDTO.notes(),
                imageUrl
        ));
    }

    @PutMapping("/{id}")
    public EmployeeModel updateEmployee(
            @PathVariable UUID id,
            @RequestPart("employeeDTO") EmployeeDTO employeeDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal OAuth2User authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        EmployeeModel existing = employeeService.getEmployeeById(id);
        String newImageUrl = imageUploadUtil.determineImageUrl(image, employeeDTO.imageUrl(), existing.getImageUrl());
        imageUploadUtil.cleanupOldImageIfNeeded(existing.getImageUrl(), newImageUrl);

        return employeeService.updateEmployee(new EmployeeModel(
                existing.getId(),
                employeeDTO.personnelNumber(),
                employeeDTO.name(),
                employeeDTO.email(),
                employeeDTO.phone(),
                employeeDTO.address(),
                employeeDTO.department(),
                employeeDTO.active(),
                employeeDTO.notes(),
                newImageUrl
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User authentication) {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        employeeService.deleteEmployee(id);
    }
}