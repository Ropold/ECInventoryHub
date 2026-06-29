package ropold.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import ropold.backend.model.AssignmentModel;
import ropold.backend.service.AssignmentService;
import ropold.backend.service.CloudinaryService;
import ropold.backend.service.ImageUploadUtil;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final CloudinaryService cloudinaryService;
    private final ImageUploadUtil imageUploadUtil;

    @GetMapping
    public List<AssignmentModel> getAllAssignments() {
        return assignmentService.findAllAssignments();
    }

    @GetMapping("/{id}")
    public AssignmentModel getAssignmentById(@PathVariable UUID id) {
        return assignmentService.getAssignmentById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssignment(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User authentication) {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        assignmentService.deleteAssignment(id);
    }
}