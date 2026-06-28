package ropold.backend.dto;

import java.util.UUID;

public record LocationDTO(
        UUID id,
        String name,
        String address,
        String phone,
        String email,
        String notes
) {}