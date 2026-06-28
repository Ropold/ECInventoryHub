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
    private final ImageUploadUtil imageUploadUtil;

    @GetMapping
    public List<EmployeeDTO> getAllEmployees() {
        return employeeService.findAllEmployees().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public EmployeeDTO getEmployeeById(@PathVariable UUID id) {
        return toDto(employeeService.getEmployeeById(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public EmployeeDTO addEmployee(
            @RequestPart("employeeDTO") EmployeeDTO employeeDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal OAuth2User authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = imageUploadUtil.determineImageUrl(image, null, null);
        }

        return toDto(employeeService.addEmployee(toModel(null, employeeDTO, imageUrl)));
    }

    @PutMapping("/{id}")
    public EmployeeDTO updateEmployee(
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

        return toDto(employeeService.updateEmployee(toModel(id, employeeDTO, newImageUrl)));
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

    private EmployeeDTO toDto(EmployeeModel model) {
        return new EmployeeDTO(
                model.getId(),
                model.getPersonnelNumber(),
                model.getName(),
                model.getEmail(),
                model.getPhone(),
                model.getAddress(),
                model.getDepartment(),
                model.isActive(),
                model.getNotes(),
                model.getImageUrl()
        );
    }

    private EmployeeModel toModel(UUID id, EmployeeDTO dto, String imageUrl) {
        return new EmployeeModel(
                id,
                dto.personnelNumber(),
                dto.name(),
                dto.email(),
                dto.phone(),
                dto.address(),
                dto.department(),
                dto.active(),
                dto.notes(),
                imageUrl
        );
    }
}