package ropold.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ropold.backend.exception.conflictexceptions.EmployeeHasAssignmentsException;
import ropold.backend.exception.notfoundexceptions.EmployeeNotFoundException;
import ropold.backend.model.AssignmentModel;
import ropold.backend.model.Department;
import ropold.backend.model.EmployeeModel;
import ropold.backend.repository.AssignmentRepository;
import ropold.backend.repository.EmployeeRepository;

import java.time.LocalDate;
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

class EmployeeServiceTest {

    EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
    EmployeeService employeeService = new EmployeeService(employeeRepository, assignmentRepository);

    List<EmployeeModel> allEmployees;

    @BeforeEach
    void setUp() {

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

        allEmployees = List.of(employeeModel1, employeeModel2);
        when(employeeRepository.findAll()).thenReturn(allEmployees);
    }

    @Test
    void getAllEmployees() {
        List<EmployeeModel> employees = employeeService.findAllEmployees();
        assertEquals(employees, allEmployees);
    }

    @Test
    void testGetEmployeeById() {
        EmployeeModel employeeModel = allEmployees.getFirst();
        when(employeeRepository.findById(employeeModel.getId())).thenReturn(Optional.of(employeeModel));
        EmployeeModel result = employeeService.getEmployeeById(employeeModel.getId());
        assertEquals(employeeModel, result);
    }

    @Test
    void testAddEmployee() {
        EmployeeModel newEmployee = new EmployeeModel(
                null,
                "P-2001",
                "New Employee",
                "new.employee@example.com",
                "+49 170 1112223",
                "Neue Strasse 3, 11111 Neustadt",
                Department.MARKETING,
                true,
                "None",
                null
        );

        EmployeeModel savedEmployee = new EmployeeModel(
                UUID.randomUUID(),
                newEmployee.getPersonnelNumber(),
                newEmployee.getName(),
                newEmployee.getEmail(),
                newEmployee.getPhone(),
                newEmployee.getAddress(),
                newEmployee.getDepartment(),
                newEmployee.isActive(),
                newEmployee.getNotes(),
                newEmployee.getImageUrl()
        );

        when(employeeRepository.save(newEmployee)).thenReturn(savedEmployee);
        EmployeeModel result = employeeService.addEmployee(newEmployee);
        assertEquals(savedEmployee, result);
    }

    @Test
    void testUpdateEmployee() {
        EmployeeModel existingEmployee = allEmployees.getFirst();
        EmployeeModel updatedEmployee = new EmployeeModel(
                existingEmployee.getId(),
                existingEmployee.getPersonnelNumber(),
                "Updated Employee Name",
                existingEmployee.getEmail(),
                existingEmployee.getPhone(),
                existingEmployee.getAddress(),
                existingEmployee.getDepartment(),
                existingEmployee.isActive(),
                existingEmployee.getNotes(),
                existingEmployee.getImageUrl()
        );

        when(employeeRepository.save(updatedEmployee)).thenReturn(updatedEmployee);

        EmployeeModel result = employeeService.updateEmployee(updatedEmployee);
        assertEquals(updatedEmployee, result);
        verify(employeeRepository, times(1)).save(updatedEmployee);
    }

    @Test
    void testDeleteEmployee() {
        EmployeeModel employeeToDelete = allEmployees.getFirst();
        when(employeeRepository.findById(employeeToDelete.getId())).thenReturn(Optional.of(employeeToDelete));
        when(assignmentRepository.findByEmployeeId(employeeToDelete.getId()))
                .thenReturn(List.of());

        employeeService.deleteEmployee(employeeToDelete.getId());
        verify(employeeRepository, times(1)).deleteById(employeeToDelete.getId());
    }

    @Test
    void testDeleteEmployee_WithBlockingAssignments_ThrowsException() {
        EmployeeModel employeeToDelete = allEmployees.getFirst();
        when(employeeRepository.findById(employeeToDelete.getId())).thenReturn(Optional.of(employeeToDelete));

        AssignmentModel assignment = new AssignmentModel();
        assignment.setId(UUID.randomUUID());
        assignment.setEmployee(employeeToDelete);
        assignment.setAssignedDate(LocalDate.of(2024, 1, 1));

        when(assignmentRepository.findByEmployeeId(employeeToDelete.getId()))
                .thenReturn(List.of(assignment));

        EmployeeHasAssignmentsException exception = assertThrows(
                EmployeeHasAssignmentsException.class,
                () -> employeeService.deleteEmployee(employeeToDelete.getId())
        );

        assertEquals(1, exception.getAssignmentDetails().size());
        assertEquals("Assignment ID: " + assignment.getId(), exception.getAssignmentDetails().getFirst());
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(nonExistentId)
        );
    }

    @Test
    void testDeleteEmployee_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.deleteEmployee(nonExistentId)
        );

        verify(employeeRepository, never()).deleteById(any());
    }
}