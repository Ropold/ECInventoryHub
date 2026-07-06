package ropold.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ropold.backend.model.AssignmentFileModel;
import ropold.backend.repository.AssignmentFileRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentFileServiceTest {

    @Mock
    private AssignmentFileRepository assignmentFileRepository;

    @InjectMocks
    private AssignmentFileService assignmentFileService;

    @Test
    void saveFile_ValidFile_ReturnsSavedFile() {
        AssignmentFileModel file = new AssignmentFileModel(
                UUID.randomUUID(),
                null,
                "https://example.com/file.pdf",
                "application/pdf",
                LocalDateTime.now()
        );
        when(assignmentFileRepository.save(file)).thenReturn(file);

        AssignmentFileModel result = assignmentFileService.saveFile(file);

        assertEquals(file, result);
        verify(assignmentFileRepository, times(1)).save(file);
    }
}