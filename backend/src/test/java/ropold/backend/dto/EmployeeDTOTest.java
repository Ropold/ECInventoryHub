package ropold.backend.dto;

import org.junit.jupiter.api.Test;
import ropold.backend.model.Department;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EmployeeDTOTest {

    @Test
    void testEmployeeDTOCreation() {
        UUID id = UUID.randomUUID();
        String personnelNumber = "P-1001";
        String name = "Max Mustermann";
        String email = "max.mustermann@example.com";
        String phone = "+49 170 1234567";
        String address = "Musterstrasse 1, 12345 Musterstadt";
        Department department = Department.DEVELOPMENT;
        boolean active = false;
        String notes = "Notes for employee one";
        String imageUrl = "https://example.com/employee1.jpg";

        EmployeeDTO employeeDTO = new EmployeeDTO(
                id, personnelNumber, name, email, phone, address, department, active, notes, imageUrl
        );

        assertEquals(id, employeeDTO.id());
        assertEquals(personnelNumber, employeeDTO.personnelNumber());
        assertEquals(name, employeeDTO.name());
        assertEquals(email, employeeDTO.email());
        assertEquals(phone, employeeDTO.phone());
        assertEquals(address, employeeDTO.address());
        assertEquals(department, employeeDTO.department());
        assertFalse(employeeDTO.active());
        assertEquals(notes, employeeDTO.notes());
        assertEquals(imageUrl, employeeDTO.imageUrl());
    }
}