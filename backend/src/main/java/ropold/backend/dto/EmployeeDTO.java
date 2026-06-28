package ropold.backend.dto;

import ropold.backend.model.Department;

import java.util.UUID;

public record EmployeeDTO(
        UUID id,
        String personnelNumber,
        String name,
        String email,
        String phone,
        String address,
        Department department,
        boolean active,
        String notes,
        String imageUrl
) {}