package ropold.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ropold.backend.model.LocationModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<LocationModel, UUID> {
    // Bei gleichem sort_order (z. B. Altdaten mit 0) entscheidet der Name
    List<LocationModel> findAllByOrderBySortOrderAscNameAsc();

    Optional<LocationModel> findTopByOrderBySortOrderDesc();
}
