package ropold.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ropold.backend.model.AssignmentModel;

import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentModel, UUID> {
}