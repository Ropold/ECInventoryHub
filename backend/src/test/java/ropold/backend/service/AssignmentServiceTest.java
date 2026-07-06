package ropold.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ropold.backend.exception.notfoundexceptions.AssignmentNotFoundException;
import ropold.backend.model.AssignmentModel;
import ropold.backend.model.Department;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.DeviceStatus;
import ropold.backend.model.DeviceType;
import ropold.backend.model.EmployeeModel;
import ropold.backend.repository.AssignmentRepository;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.EmployeeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignmentServiceTest {

    AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
    DeviceRepository deviceRepository = mock(DeviceRepository.class);
    EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    AssignmentService assignmentService = new AssignmentService(assignmentRepository, deviceRepository, employeeRepository);

    List<AssignmentModel> allAssignments;

    @BeforeEach
    void setUp() {

        DeviceModel deviceModel1 = new DeviceModel(
                UUID.randomUUID(),
                DeviceType.LAPTOP,
                "Dell",
                "Latitude 5420",
                "SN-1001",
                "INV-1001",
                LocalDate.of(2023, 1, 15),
                DeviceStatus.ASSIGNED,
                false,
                null,
                "Notes for device one",
                new ArrayList<>()
        );

        EmployeeModel employeeModel1 = new EmployeeModel(
                UUID.randomUUID(),
                "P-1001",
                "Max Mustermann",
                "max.mustermann@example.com",
                "+49 170 1234567",
                "Musterstrasse 1, 12345 Musterstadt",
                Department.DEVELOPMENT,
                true,
                "Notes for employee one",
                "http://example.com/employee1.jpg"
        );

        EmployeeModel employeeModel2 = new EmployeeModel(
                UUID.randomUUID(),
                "P-1002",
                "Erika Musterfrau",
                "erika.musterfrau@example.com",
                "+49 170 7654321",
                "Beispielweg 2, 54321 Beispielstadt",
                Department.HR,
                false,
                "Notes for employee two",
                "http://example.com/employee2.jpg"
        );

        AssignmentModel assignmentModel1 = new AssignmentModel(
                UUID.randomUUID(),
                deviceModel1,
                employeeModel1,
                employeeModel2,
                LocalDate.of(2024, 1, 1),
                null,
                "Like new",
                null,
                "Notes for assignment one",
                true,
                false,
                new ArrayList<>()
        );

        AssignmentModel assignmentModel2 = new AssignmentModel(
                UUID.randomUUID(),
                deviceModel1,
                employeeModel2,
                employeeModel1,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 3, 1),
                "Good condition",
                "Minor scratches",
                "Notes for assignment two",
                false,
                true,
                new ArrayList<>()
        );

        allAssignments = List.of(assignmentModel1, assignmentModel2);
        when(assignmentRepository.findAll()).thenReturn(allAssignments);
    }

    @Test
    void getAllAssignments() {
        List<AssignmentModel> assignments = assignmentService.findAllAssignments();
        assertEquals(assignments, allAssignments);
    }

    @Test
    void testGetAssignmentById() {
        AssignmentModel assignmentModel = allAssignments.getFirst();
        when(assignmentRepository.findById(assignmentModel.getId())).thenReturn(Optional.of(assignmentModel));
        AssignmentModel result = assignmentService.getAssignmentById(assignmentModel.getId());
        assertEquals(assignmentModel, result);
    }

    @Test
    void testGetAssignmentById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(assignmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                AssignmentNotFoundException.class,
                () -> assignmentService.getAssignmentById(nonExistentId)
        );
    }

    @Test
    void testDeleteAssignment() {
        AssignmentModel assignmentToDelete = allAssignments.getFirst();
        when(assignmentRepository.findById(assignmentToDelete.getId())).thenReturn(Optional.of(assignmentToDelete));
        assignmentService.deleteAssignment(assignmentToDelete.getId());
        verify(assignmentRepository, times(1)).deleteById(assignmentToDelete.getId());
    }

    @Test
    void testDeleteAssignment_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(assignmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                AssignmentNotFoundException.class,
                () -> assignmentService.deleteAssignment(nonExistentId)
        );

        verify(assignmentRepository, never()).deleteById(any());
    }
}