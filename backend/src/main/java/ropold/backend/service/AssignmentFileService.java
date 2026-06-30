package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.model.AssignmentFileModel;
import ropold.backend.repository.AssignmentFileRepository;

@Service
@RequiredArgsConstructor
public class AssignmentFileService {
    private final AssignmentFileRepository assignmentFileRepository;

    public AssignmentFileModel saveFile(AssignmentFileModel file) {
        return assignmentFileRepository.save(file);
    }
}