package ropold.backend.dto;

import org.junit.jupiter.api.Test;
import ropold.backend.model.Department;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssignmentDTOTest {

    @Test
    void testAssignmentDTOCreation() {
        UUID id = UUID.randomUUID();
        DeviceDTO device = new DeviceDTO(
                UUID.randomUUID(), null, "Dell", "Latitude 5420", "SN-1001", "INV-1001",
                null, null, false, null, "Notes", null
        );
        EmployeeDTO employee = new EmployeeDTO(
                UUID.randomUUID(), "P-1001", "Max Mustermann", "max.mustermann@example.com",
                "+49 170 1234567", "Musterstrasse 1, 12345 Musterstadt", Department.DEVELOPMENT,
                true, "Notes", "https://example.com/employee1.jpg"
        );
        EmployeeDTO handedOutBy = new EmployeeDTO(
                UUID.randomUUID(), "P-1002", "Erika Musterfrau", "erika.musterfrau@example.com",
                "+49 170 7654321", "Beispielweg 2, 54321 Beispielstadt", Department.HR,
                false, "Notes", "https://example.com/employee2.jpg"
        );
        LocalDate assignedDate = LocalDate.of(2024, 1, 1);
        LocalDate returnedDate = LocalDate.of(2024, 3, 1);
        String conditionOut = "Like new";
        String conditionIn = "Minor scratches";
        String notes = "Notes for assignment one";
        boolean copyHandedToEmployee = true;
        boolean copyFiledInPersonnelFile = false;
        List<AssignmentFileDTO> files = List.of(
                new AssignmentFileDTO(UUID.randomUUID(), "https://example.com/file.pdf", "application/pdf", null)
        );

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                id, device, employee, handedOutBy, assignedDate, returnedDate,
                conditionOut, conditionIn, notes, copyHandedToEmployee, copyFiledInPersonnelFile, files
        );

        assertEquals(id, assignmentDTO.id());
        assertEquals(device, assignmentDTO.device());
        assertEquals(employee, assignmentDTO.employee());
        assertEquals(handedOutBy, assignmentDTO.handedOutBy());
        assertEquals(assignedDate, assignmentDTO.assignedDate());
        assertEquals(returnedDate, assignmentDTO.returnedDate());
        assertEquals(conditionOut, assignmentDTO.conditionOut());
        assertEquals(conditionIn, assignmentDTO.conditionIn());
        assertEquals(notes, assignmentDTO.notes());
        assertTrue(assignmentDTO.copyHandedToEmployee());
        assertFalse(assignmentDTO.copyFiledInPersonnelFile());
        assertEquals(files, assignmentDTO.files());
    }
}