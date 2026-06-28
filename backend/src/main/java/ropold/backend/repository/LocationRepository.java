package ropold.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ropold.backend.model.LocationModel;

import java.util.UUID;

public interface LocationRepository extends JpaRepository<LocationModel, UUID> {
}