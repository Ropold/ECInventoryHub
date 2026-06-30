package ropold.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ropold.backend.dto.AssignmentDTO;
import ropold.backend.dto.AssignmentFileDTO;
import ropold.backend.model.AssignmentFileModel;
import ropold.backend.model.AssignmentModel;
import ropold.backend.service.AssignmentFileService;
import ropold.backend.service.AssignmentService;
import ropold.backend.service.CloudinaryService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final AssignmentFileService assignmentFileService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public List<AssignmentModel> getAllAssignments() {
        return assignmentService.findAllAssignments();
    }

    @GetMapping("/{id}")
    public AssignmentModel getAssignmentById(@PathVariable UUID id) {
        return assignmentService.getAssignmentById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentModel addAssignment(
            @RequestPart("assignmentDTO") AssignmentDTO assignmentDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal OAuth2User authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        AssignmentModel saved = assignmentService.addAssignment(assignmentDTO);

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = uploadFile(file);
                    assignmentFileService.saveFile(new AssignmentFileModel(
                            null, saved, fileUrl, file.getContentType(), LocalDateTime.now()
                    ));
                }
            }
        }

        return assignmentService.getAssignmentById(saved.getId());
    }

    @PutMapping("/{id}")
    public AssignmentModel updateAssignment(
            @PathVariable UUID id,
            @RequestPart("assignmentDTO") AssignmentDTO assignmentDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal OAuth2User authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        Set<UUID> keepIds = assignmentDTO.files() != null
                ? assignmentDTO.files().stream().map(AssignmentFileDTO::id).collect(Collectors.toSet())
                : Collections.emptySet();

        AssignmentModel existing = assignmentService.getAssignmentById(id);
        for (AssignmentFileModel existingFile : existing.getFiles()) {
            if (!keepIds.contains(existingFile.getId())) {
                deleteFileFromCloudinary(existingFile);
            }
        }

        AssignmentModel updated = assignmentService.updateAssignment(id, assignmentDTO);

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = uploadFile(file);
                    assignmentFileService.saveFile(new AssignmentFileModel(
                            null, updated, fileUrl, file.getContentType(), LocalDateTime.now()
                    ));
                }
            }
        }

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

    private String uploadFile(MultipartFile file) throws IOException {
        if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
            return cloudinaryService.uploadImage(file);
        }
        return cloudinaryService.uploadFile(file);
    }

    private void deleteFileFromCloudinary(AssignmentFileModel file) {
        if (file.getFileType() != null && file.getFileType().startsWith("image/")) {
            cloudinaryService.deleteImage(file.getFileUrl());
        } else {
            cloudinaryService.deleteFile(file.getFileUrl());
        }
    }
}