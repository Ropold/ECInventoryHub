package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.dto.AssignmentDTO;
import ropold.backend.dto.AssignmentFileDTO;
import ropold.backend.exception.notfoundexceptions.AssignmentNotFoundException;
import ropold.backend.exception.notfoundexceptions.DeviceNotFoundException;
import ropold.backend.exception.notfoundexceptions.EmployeeNotFoundException;
import ropold.backend.model.AssignmentModel;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.EmployeeModel;
import ropold.backend.repository.AssignmentRepository;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.EmployeeRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;
    private final EmployeeRepository employeeRepository;

    public List<AssignmentModel> findAllAssignments() {
        return assignmentRepository.findAll();
    }

    public AssignmentModel getAssignmentById(UUID id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with id: " + id));
    }

    public AssignmentModel addAssignment(AssignmentDTO dto) {
        DeviceModel device = deviceRepository.findById(dto.device().id())
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + dto.device().id()));
        EmployeeModel employee = employeeRepository.findById(dto.employee().id())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + dto.employee().id()));

        EmployeeModel handedOutBy = null;
        if (dto.handedOutBy() != null && dto.handedOutBy().id() != null) {
            handedOutBy = employeeRepository.findById(dto.handedOutBy().id())
                    .orElseThrow(() -> new EmployeeNotFoundException("HandedOutBy employee not found with id: " + dto.handedOutBy().id()));
        }

        return assignmentRepository.save(new AssignmentModel(
                null,
                device,
                employee,
                handedOutBy,
                dto.assignedDate(),
                dto.returnedDate(),
                dto.conditionOut(),
                dto.conditionIn(),
                dto.notes(),
                dto.copyHandedToEmployee(),
                dto.copyFiledInPersonnelFile(),
                new ArrayList<>()
        ));
    }

    public AssignmentModel updateAssignment(UUID id, AssignmentDTO dto) {
        AssignmentModel existing = getAssignmentById(id);

        DeviceModel device = deviceRepository.findById(dto.device().id())
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + dto.device().id()));
        EmployeeModel employee = employeeRepository.findById(dto.employee().id())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + dto.employee().id()));

        EmployeeModel handedOutBy = null;
        if (dto.handedOutBy() != null && dto.handedOutBy().id() != null) {
            handedOutBy = employeeRepository.findById(dto.handedOutBy().id())
                    .orElseThrow(() -> new EmployeeNotFoundException("HandedOutBy employee not found with id: " + dto.handedOutBy().id()));
        }

        existing.setDevice(device);
        existing.setEmployee(employee);
        existing.setHandedOutBy(handedOutBy);
        existing.setAssignedDate(dto.assignedDate());
        existing.setReturnedDate(dto.returnedDate());
        existing.setConditionOut(dto.conditionOut());
        existing.setConditionIn(dto.conditionIn());
        existing.setNotes(dto.notes());
        existing.setCopyHandedToEmployee(dto.copyHandedToEmployee());
        existing.setCopyFiledInPersonnelFile(dto.copyFiledInPersonnelFile());

        Set<UUID> keepIds = dto.files() != null
                ? dto.files().stream().map(AssignmentFileDTO::id).collect(Collectors.toSet())
                : Collections.emptySet();
        existing.getFiles().removeIf(f -> !keepIds.contains(f.getId()));

        return assignmentRepository.save(existing);
    }

    public void deleteAssignment(UUID id) {
        assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with id: " + id));
        assignmentRepository.deleteById(id);
    }
}