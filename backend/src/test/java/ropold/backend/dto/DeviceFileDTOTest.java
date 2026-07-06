package ropold.backend.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeviceFileDTOTest {

    @Test
    void testDeviceFileDTOCreation() {
        UUID id = UUID.randomUUID();
        String fileUrl = "https://example.com/file.pdf";
        String fileType = "application/pdf";
        LocalDateTime uploadedAt = LocalDateTime.of(2024, 1, 1, 12, 0);

        DeviceFileDTO deviceFileDTO = new DeviceFileDTO(id, fileUrl, fileType, uploadedAt);

        assertEquals(id, deviceFileDTO.id());
        assertEquals(fileUrl, deviceFileDTO.fileUrl());
        assertEquals(fileType, deviceFileDTO.fileType());
        assertEquals(uploadedAt, deviceFileDTO.uploadedAt());
    }
}