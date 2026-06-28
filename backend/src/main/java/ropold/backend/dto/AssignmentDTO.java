package ropold.backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AssignmentDTO(
        UUID id,
        DeviceDTO device,
        EmployeeDTO employee,
        EmployeeDTO handedOutBy,
        LocalDate assignedDate,
        LocalDate returnedDate,
        String conditionOut,
        String conditionIn,
        String notes,
        boolean copyHandedToEmployee,
        boolean copyFiledInPersonnelFile,
        List<AssignmentFileDTO> files
) {}