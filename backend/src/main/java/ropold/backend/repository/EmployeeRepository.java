package ropold.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ropold.backend.model.EmployeeModel;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<EmployeeModel, UUID> {
}