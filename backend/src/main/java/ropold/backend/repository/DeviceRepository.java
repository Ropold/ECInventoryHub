package ropold.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ropold.backend.model.DeviceModel;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<DeviceModel, UUID> {
}