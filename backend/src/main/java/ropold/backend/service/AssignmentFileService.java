package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.repository.AssignmentFileRepository;

@Service
@RequiredArgsConstructor
public class AssignmentFileService {
    private final AssignmentFileRepository assignmentFileRepository;
}