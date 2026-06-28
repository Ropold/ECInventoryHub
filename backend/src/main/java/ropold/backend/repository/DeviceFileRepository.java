package ropold.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ropold.backend.model.DeviceFileModel;

import java.util.UUID;

public interface DeviceFileRepository extends JpaRepository<DeviceFileModel, UUID> {
}