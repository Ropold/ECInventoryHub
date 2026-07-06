package ropold.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ropold.backend.model.DeviceFileModel;
import ropold.backend.repository.DeviceFileRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceFileServiceTest {

    @Mock
    private DeviceFileRepository deviceFileRepository;

    @InjectMocks
    private DeviceFileService deviceFileService;

    @Test
    void saveFile_ValidFile_ReturnsSavedFile() {
        DeviceFileModel file = new DeviceFileModel(
                UUID.randomUUID(),
                null,
                "https://example.com/file.pdf",
                "application/pdf",
                LocalDateTime.now()
        );
        when(deviceFileRepository.save(file)).thenReturn(file);

        DeviceFileModel result = deviceFileService.saveFile(file);

        assertEquals(file, result);
        verify(deviceFileRepository, times(1)).save(file);
    }
}