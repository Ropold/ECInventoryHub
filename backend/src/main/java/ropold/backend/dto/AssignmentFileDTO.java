package ropold.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AssignmentFileDTO(
        UUID id,
        String fileUrl,
        String fileType,
        LocalDateTime uploadedAt
) {}