package ropold.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ropold.backend.model.AssignmentFileModel;

import java.util.UUID;

public interface AssignmentFileRepository extends JpaRepository<AssignmentFileModel, UUID> {
}