package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.exception.conflictexceptions.EmployeeHasAssignmentsException;
import ropold.backend.exception.notfoundexceptions.EmployeeNotFoundException;
import ropold.backend.model.AssignmentModel;
import ropold.backend.model.EmployeeModel;
import ropold.backend.repository.AssignmentRepository;
import ropold.backend.repository.EmployeeRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AssignmentRepository assignmentRepository;

    public List<EmployeeModel> findAllEmployees() {
        return employeeRepository.findAll();
    }

    public EmployeeModel getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
    }

    public EmployeeModel addEmployee(EmployeeModel employeeModel) {
        return employeeRepository.save(employeeModel);
    }

    public EmployeeModel updateEmployee(EmployeeModel employeeModel) {
        return employeeRepository.save(employeeModel);
    }

    public void deleteEmployee(UUID id) {
        employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        List<AssignmentModel> blockingAssignments = assignmentRepository.findByEmployeeId(id);
        if (!blockingAssignments.isEmpty()) {
            throw new EmployeeHasAssignmentsException(
                    "Employee cannot be deleted because there are still assignments referencing them.",
                    blockingAssignments.stream().map(EmployeeService::describeAssignment).toList());
        }

        employeeRepository.deleteById(id);
    }

    public void forceDeleteEmployee(UUID id) {
        employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        List<AssignmentModel> assignmentsToDelete = assignmentRepository.findByEmployeeId(id);
        assignmentRepository.deleteAll(assignmentsToDelete);

        employeeRepository.deleteById(id);
    }

    private static String describeAssignment(AssignmentModel assignment) {
        return "Assignment ID: " + assignment.getId();
    }
}