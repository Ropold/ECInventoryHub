package ropold.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ropold.backend.model.AssignmentModel;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentModel, UUID> {
    List<AssignmentModel> findByEmployeeId(UUID employeeId);

    List<AssignmentModel> findByHandedOutById(UUID handedOutById);

    List<AssignmentModel> findByDeviceId(UUID deviceId);
}
