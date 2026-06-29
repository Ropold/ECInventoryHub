package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ropold.backend.exception.notfoundexceptions.AssignmentNotFoundException;
import ropold.backend.model.AssignmentModel;
import ropold.backend.repository.AssignmentRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;

    public List<AssignmentModel> findAllAssignments() {
        return assignmentRepository.findAll();
    }

    public AssignmentModel getAssignmentById(UUID id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with id: " + id));
    }

    public void deleteAssignment(UUID id) {
        assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with id: " + id));
        assignmentRepository.deleteById(id);
    }
}