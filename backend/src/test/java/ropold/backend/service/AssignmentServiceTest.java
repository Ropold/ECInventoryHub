package ropold.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ropold.backend.dto.AssignmentDTO;
import ropold.backend.dto.DeviceDTO;
import ropold.backend.dto.EmployeeDTO;
import ropold.backend.exception.notfoundexceptions.AssignmentNotFoundException;
import ropold.backend.exception.notfoundexceptions.DeviceNotFoundException;
import ropold.backend.exception.notfoundexceptions.EmployeeNotFoundException;
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
    DeviceModel deviceModel1;
    EmployeeModel employeeModel1;
    EmployeeModel employeeModel2;

    @BeforeEach
    void setUp() {

        deviceModel1 = new DeviceModel(
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

        employeeModel1 = new EmployeeModel(
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

        employeeModel2 = new EmployeeModel(
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
    void testAddAssignment() {
        DeviceDTO deviceDTO = toDeviceDTO(deviceModel1);
        EmployeeDTO employeeDTO = toEmployeeDTO(employeeModel1);
        EmployeeDTO handedOutByDTO = toEmployeeDTO(employeeModel2);

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                null,
                deviceDTO,
                employeeDTO,
                handedOutByDTO,
                LocalDate.of(2024, 5, 1),
                null,
                "New condition",
                null,
                "Notes for new assignment",
                true,
                false,
                null
        );

        when(deviceRepository.findById(deviceModel1.getId())).thenReturn(Optional.of(deviceModel1));
        when(employeeRepository.findById(employeeModel1.getId())).thenReturn(Optional.of(employeeModel1));
        when(employeeRepository.findById(employeeModel2.getId())).thenReturn(Optional.of(employeeModel2));
        when(assignmentRepository.save(any(AssignmentModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentModel result = assignmentService.addAssignment(assignmentDTO);

        assertEquals(deviceModel1, result.getDevice());
        assertEquals(employeeModel1, result.getEmployee());
        assertEquals(employeeModel2, result.getHandedOutBy());
        assertEquals(assignmentDTO.assignedDate(), result.getAssignedDate());
        assertEquals(assignmentDTO.notes(), result.getNotes());
        verify(assignmentRepository, times(1)).save(any(AssignmentModel.class));
    }

    @Test
    void testAddAssignment_DeviceNotFound() {
        DeviceDTO deviceDTO = toDeviceDTO(deviceModel1);
        EmployeeDTO employeeDTO = toEmployeeDTO(employeeModel1);

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                null, deviceDTO, employeeDTO, null,
                LocalDate.of(2024, 5, 1), null, null, null, null,
                false, false, null
        );

        when(deviceRepository.findById(deviceModel1.getId())).thenReturn(Optional.empty());

        assertThrows(
                DeviceNotFoundException.class,
                () -> assignmentService.addAssignment(assignmentDTO)
        );

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void testAddAssignment_EmployeeNotFound() {
        DeviceDTO deviceDTO = toDeviceDTO(deviceModel1);
        EmployeeDTO employeeDTO = toEmployeeDTO(employeeModel1);

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                null, deviceDTO, employeeDTO, null,
                LocalDate.of(2024, 5, 1), null, null, null, null,
                false, false, null
        );

        when(deviceRepository.findById(deviceModel1.getId())).thenReturn(Optional.of(deviceModel1));
        when(employeeRepository.findById(employeeModel1.getId())).thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> assignmentService.addAssignment(assignmentDTO)
        );

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void testAddAssignment_HandedOutByNotFound() {
        DeviceDTO deviceDTO = toDeviceDTO(deviceModel1);
        EmployeeDTO employeeDTO = toEmployeeDTO(employeeModel1);
        EmployeeDTO handedOutByDTO = toEmployeeDTO(employeeModel2);

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                null, deviceDTO, employeeDTO, handedOutByDTO,
                LocalDate.of(2024, 5, 1), null, null, null, null,
                false, false, null
        );

        when(deviceRepository.findById(deviceModel1.getId())).thenReturn(Optional.of(deviceModel1));
        when(employeeRepository.findById(employeeModel1.getId())).thenReturn(Optional.of(employeeModel1));
        when(employeeRepository.findById(employeeModel2.getId())).thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> assignmentService.addAssignment(assignmentDTO)
        );

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void testUpdateAssignment() {
        AssignmentModel existing = allAssignments.getFirst();

        DeviceDTO deviceDTO = toDeviceDTO(deviceModel1);
        EmployeeDTO employeeDTO = toEmployeeDTO(employeeModel2);
        EmployeeDTO handedOutByDTO = toEmployeeDTO(employeeModel1);

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                existing.getId(),
                deviceDTO,
                employeeDTO,
                handedOutByDTO,
                existing.getAssignedDate(),
                LocalDate.of(2024, 6, 1),
                existing.getConditionOut(),
                "Returned with minor damage",
                "Updated notes",
                existing.isCopyHandedToEmployee(),
                existing.isCopyFiledInPersonnelFile(),
                null
        );

        when(assignmentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(deviceRepository.findById(deviceModel1.getId())).thenReturn(Optional.of(deviceModel1));
        when(employeeRepository.findById(employeeModel2.getId())).thenReturn(Optional.of(employeeModel2));
        when(employeeRepository.findById(employeeModel1.getId())).thenReturn(Optional.of(employeeModel1));
        when(assignmentRepository.save(existing)).thenReturn(existing);

        AssignmentModel result = assignmentService.updateAssignment(existing.getId(), assignmentDTO);

        assertEquals(employeeModel2, result.getEmployee());
        assertEquals(employeeModel1, result.getHandedOutBy());
        assertEquals("Updated notes", result.getNotes());
        assertEquals("Returned with minor damage", result.getConditionIn());
        verify(assignmentRepository, times(1)).save(existing);
    }

    @Test
    void testUpdateAssignment_AssignmentNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        DeviceDTO deviceDTO = toDeviceDTO(deviceModel1);
        EmployeeDTO employeeDTO = toEmployeeDTO(employeeModel1);
        AssignmentDTO assignmentDTO = new AssignmentDTO(
                nonExistentId, deviceDTO, employeeDTO, null,
                LocalDate.now(), null, null, null, null,
                false, false, null
        );

        when(assignmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                AssignmentNotFoundException.class,
                () -> assignmentService.updateAssignment(nonExistentId, assignmentDTO)
        );

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void testUpdateAssignment_DeviceNotFound() {
        AssignmentModel existing = allAssignments.getFirst();
        DeviceDTO deviceDTO = toDeviceDTO(deviceModel1);
        EmployeeDTO employeeDTO = toEmployeeDTO(employeeModel1);
        AssignmentDTO assignmentDTO = new AssignmentDTO(
                existing.getId(), deviceDTO, employeeDTO, null,
                existing.getAssignedDate(), null, null, null, null,
                false, false, null
        );

        when(assignmentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(deviceRepository.findById(deviceModel1.getId())).thenReturn(Optional.empty());

        assertThrows(
                DeviceNotFoundException.class,
                () -> assignmentService.updateAssignment(existing.getId(), assignmentDTO)
        );

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void testUpdateAssignment_EmployeeNotFound() {
        AssignmentModel existing = allAssignments.getFirst();
        DeviceDTO deviceDTO = toDeviceDTO(deviceModel1);
        EmployeeDTO employeeDTO = toEmployeeDTO(employeeModel1);
        AssignmentDTO assignmentDTO = new AssignmentDTO(
                existing.getId(), deviceDTO, employeeDTO, null,
                existing.getAssignedDate(), null, null, null, null,
                false, false, null
        );

        when(assignmentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(deviceRepository.findById(deviceModel1.getId())).thenReturn(Optional.of(deviceModel1));
        when(employeeRepository.findById(employeeModel1.getId())).thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> assignmentService.updateAssignment(existing.getId(), assignmentDTO)
        );

        verify(assignmentRepository, never()).save(any());
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

    private DeviceDTO toDeviceDTO(DeviceModel device) {
        return new DeviceDTO(
                device.getId(),
                device.getType(),
                device.getManufacturer(),
                device.getModelName(),
                device.getSerialNumber(),
                device.getInventoryNumber(),
                device.getPurchaseDate(),
                device.getStatus(),
                device.isDefective(),
                null,
                device.getNotes(),
                null
        );
    }

    private EmployeeDTO toEmployeeDTO(EmployeeModel employee) {
        return new EmployeeDTO(
                employee.getId(),
                employee.getPersonnelNumber(),
                employee.getName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getAddress(),
                employee.getDepartment(),
                employee.isActive(),
                employee.getNotes(),
                employee.getImageUrl()
        );
    }
}