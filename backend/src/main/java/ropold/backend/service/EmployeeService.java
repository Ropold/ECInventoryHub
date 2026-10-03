package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.exception.conflictexceptions.EmployeeHasAssignmentsException;
import ropold.backend.exception.notfoundexceptions.EmployeeNotFoundException;
import ropold.backend.model.AssignmentModel;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.DeviceStatus;
import ropold.backend.model.EmployeeModel;
import ropold.backend.repository.AssignmentRepository;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.EmployeeRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;

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

        List<AssignmentModel> blockingAssignments = Stream.concat(
                        assignmentRepository.findByEmployeeId(id).stream(),
                        assignmentRepository.findByHandedOutById(id).stream())
                .distinct()
                .toList();
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

        // Geräte aus gelöschten offenen Zuweisungen sind wieder frei (pro Gerät gibt es nur eine offene Zuweisung)
        List<DeviceModel> freedDevices = assignmentsToDelete.stream()
                .filter(assignment -> assignment.getReturnedDate() == null)
                .map(AssignmentModel::getDevice)
                .filter(Objects::nonNull)
                .filter(device -> device.getStatus() == DeviceStatus.ASSIGNED)
                .toList();
        freedDevices.forEach(device -> device.setStatus(DeviceStatus.AVAILABLE));
        deviceRepository.saveAll(freedDevices);

        // Assignments this employee only handed out are kept, just without the "handed out by" reference
        List<AssignmentModel> assignmentsToDetach = assignmentRepository.findByHandedOutById(id).stream()
                .filter(assignment -> !assignmentsToDelete.contains(assignment))
                .toList();
        assignmentsToDetach.forEach(assignment -> assignment.setHandedOutBy(null));
        assignmentRepository.saveAll(assignmentsToDetach);

        employeeRepository.deleteById(id);
    }

    private static String describeAssignment(AssignmentModel assignment) {
        return "Assignment ID: " + assignment.getId();
    }
}