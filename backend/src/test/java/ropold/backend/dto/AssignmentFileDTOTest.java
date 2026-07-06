package ropold.backend.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssignmentFileDTOTest {

    @Test
    void testAssignmentFileDTOCreation() {
        UUID id = UUID.randomUUID();
        String fileUrl = "https://example.com/file.pdf";
        String fileType = "application/pdf";
        LocalDateTime uploadedAt = LocalDateTime.of(2024, 1, 1, 12, 0);

        AssignmentFileDTO assignmentFileDTO = new AssignmentFileDTO(id, fileUrl, fileType, uploadedAt);

        assertEquals(id, assignmentFileDTO.id());
        assertEquals(fileUrl, assignmentFileDTO.fileUrl());
        assertEquals(fileType, assignmentFileDTO.fileType());
        assertEquals(uploadedAt, assignmentFileDTO.uploadedAt());
    }
}