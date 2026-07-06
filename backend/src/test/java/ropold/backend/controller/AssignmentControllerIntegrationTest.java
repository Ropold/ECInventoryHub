package ropold.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import jakarta.persistence.EntityManager;
import ropold.backend.model.AssignmentFileModel;
import ropold.backend.model.AssignmentModel;
import ropold.backend.model.Department;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.DeviceStatus;
import ropold.backend.model.DeviceType;
import ropold.backend.model.EmployeeModel;
import ropold.backend.repository.AssignmentFileRepository;
import ropold.backend.repository.AssignmentRepository;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.EmployeeRepository;
import ropold.backend.service.CloudinaryService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssignmentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AssignmentFileRepository assignmentFileRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private DeviceModel deviceModel1;
    private EmployeeModel employeeModel1;
    private EmployeeModel employeeModel2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Delete in correct order to respect foreign key constraints
        assignmentRepository.deleteAll();
        deviceRepository.deleteAll();
        employeeRepository.deleteAll();

        deviceModel1 = deviceRepository.save(new DeviceModel(
                null,
                DeviceType.LAPTOP,
                "Dell",
                "Latitude 5420",
                "SN-1001",
                "INV-1001",
                LocalDate.of(2023, 1, 15),
                DeviceStatus.ASSIGNED,
                false,
                null,
                "Notes for device one",
                new ArrayList<>()
        ));

        employeeModel1 = employeeRepository.save(new EmployeeModel(
                null,
                "P-1001",
                "Max Mustermann",
                "max.mustermann@example.com",
                "+49 170 1234567",
                "Musterstrasse 1, 12345 Musterstadt",
                Department.DEVELOPMENT,
                true,
                "Notes for employee one",
                "https://example.com/employee1.jpg"
        ));

        employeeModel2 = employeeRepository.save(new EmployeeModel(
                null,
                "P-1002",
                "Erika Musterfrau",
                "erika.musterfrau@example.com",
                "+49 170 7654321",
                "Beispielweg 2, 54321 Beispielstadt",
                Department.HR,
                false,
                "Notes for employee two",
                "https://example.com/employee2.jpg"
        ));

        AssignmentModel assignmentModel1 = new AssignmentModel(
                null,
                deviceModel1,
                employeeModel1,
                employeeModel2,
                LocalDate.of(2024, 1, 1),
                null,
                "Like new",
                null,
                "Notes for assignment one",
                true,
                false,
                new ArrayList<>()
        );

        AssignmentModel assignmentModel2 = new AssignmentModel(
                null,
                deviceModel1,
                employeeModel2,
                employeeModel1,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 3, 1),
                "Good condition",
                "Minor scratches",
                "Notes for assignment two",
                false,
                true,
                new ArrayList<>()
        );

        assignmentRepository.saveAll(List.of(assignmentModel1, assignmentModel2));
    }

    @Test
    void testGetAllAssignments() throws Exception {
        mockMvc.perform(get("/api/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notes").value("Notes for assignment one"))
                .andExpect(jsonPath("$[1].notes").value("Notes for assignment two"));
    }

    @Test
    void testGetAssignmentById() throws Exception {
        AssignmentModel savedAssignment = assignmentRepository.findAll().getFirst();

        mockMvc.perform(get("/api/assignments/" + savedAssignment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Notes for assignment one"))
                .andExpect(jsonPath("$.conditionOut").value("Like new"));
    }

    @Test
    void testGetAssignmentById_notFound() throws Exception {
        mockMvc.perform(get("/api/assignments/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddAssignment_shouldReturnCreated() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        assignmentRepository.deleteAll();

        String newAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "handedOutBy": { "id": "%s", "active": false },
                    "assignedDate": "2024-05-01",
                    "conditionOut": "New condition",
                    "notes": "Notes for new assignment",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId(), employeeModel2.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", newAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/assignments")
                        .file(assignmentDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").value("Notes for new assignment"))
                .andExpect(jsonPath("$.conditionOut").value("New condition"));

        List<AssignmentModel> allAssignments = assignmentRepository.findAll();
        assertEquals(1, allAssignments.size());
    }

    @Test
    void testAddAssignment_unauthenticated_shouldReturnUnauthorized() throws Exception {
        String newAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-05-01",
                    "conditionOut": "New condition",
                    "notes": "Notes for new assignment",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", newAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/assignments")
                        .file(assignmentDtoPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testAddAssignment_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        String newAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-05-01",
                    "conditionOut": "New condition",
                    "notes": "Notes for new assignment",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", newAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/assignments")
                        .file(assignmentDtoPart))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testAddAssignment_withImageFile_shouldUploadImageAndSaveFile() throws Exception {
        when(cloudinaryService.uploadImage(any())).thenReturn("https://cloudinary.test/new-image.png");

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        assignmentRepository.deleteAll();

        String newAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-05-01",
                    "conditionOut": "New condition",
                    "notes": "Notes for new assignment",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", newAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile imageFilePart = new MockMultipartFile(
                "files", "photo.png", "image/png", "image-bytes".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/assignments")
                        .file(assignmentDtoPart)
                        .file(imageFilePart)
                        .with(authentication(authToken)))
                .andExpect(status().isCreated());

        verify(cloudinaryService).uploadImage(any());
        verify(cloudinaryService, never()).uploadFile(any());

        List<AssignmentFileModel> savedFiles = assignmentFileRepository.findAll();
        assertEquals(1, savedFiles.size());
        assertEquals("https://cloudinary.test/new-image.png", savedFiles.getFirst().getFileUrl());
        assertEquals("image/png", savedFiles.getFirst().getFileType());
    }

    @Test
    void testAddAssignment_withNonImageFile_shouldUploadFileAndSaveFile() throws Exception {
        when(cloudinaryService.uploadFile(any())).thenReturn("https://cloudinary.test/new-doc.pdf");

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        assignmentRepository.deleteAll();

        String newAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-05-01",
                    "conditionOut": "New condition",
                    "notes": "Notes for new assignment",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", newAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile docFilePart = new MockMultipartFile(
                "files", "protocol.pdf", "application/pdf", "pdf-bytes".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/assignments")
                        .file(assignmentDtoPart)
                        .file(docFilePart)
                        .with(authentication(authToken)))
                .andExpect(status().isCreated());

        verify(cloudinaryService).uploadFile(any());
        verify(cloudinaryService, never()).uploadImage(any());

        List<AssignmentFileModel> savedFiles = assignmentFileRepository.findAll();
        assertEquals(1, savedFiles.size());
        assertEquals("https://cloudinary.test/new-doc.pdf", savedFiles.getFirst().getFileUrl());
    }

    @Test
    void testAddAssignment_withEmptyFilePart_shouldNotUploadAnything() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        assignmentRepository.deleteAll();

        String newAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-05-01",
                    "conditionOut": "New condition",
                    "notes": "Notes for new assignment",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", newAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile emptyFilePart = new MockMultipartFile(
                "files", "empty.txt", "text/plain", new byte[0]
        );

        mockMvc.perform(multipart("/api/assignments")
                        .file(assignmentDtoPart)
                        .file(emptyFilePart)
                        .with(authentication(authToken)))
                .andExpect(status().isCreated());

        verify(cloudinaryService, never()).uploadImage(any());
        verify(cloudinaryService, never()).uploadFile(any());
        assertTrue(assignmentFileRepository.findAll().isEmpty());
    }

    @Test
    void testUpdateAssignment_shouldReturnUpdated() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();

        // No "files" part sent and assignmentDTO.files() omitted -> keepIds is empty,
        // but the existing assignment has no files persisted, so no Cloudinary cleanup call happens.
        String updatedAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": false },
                    "handedOutBy": { "id": "%s", "active": true },
                    "assignedDate": "2024-01-01",
                    "returnedDate": "2024-06-01",
                    "conditionOut": "Like new",
                    "conditionIn": "Returned with minor damage",
                    "notes": "Updated notes",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel2.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", updatedAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/assignments/" + existingAssignment.getId())
                        .file(assignmentDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Updated notes"))
                .andExpect(jsonPath("$.conditionIn").value("Returned with minor damage"));

        AssignmentModel updated = assignmentRepository.findById(existingAssignment.getId()).orElseThrow();
        assertEquals("Updated notes", updated.getNotes());
        assertEquals(employeeModel2.getId(), updated.getEmployee().getId());
    }

    @Test
    void testUpdateAssignment_unauthenticated_shouldReturnUnauthorized() throws Exception {
        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();

        String updatedAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-01-01",
                    "conditionOut": "Like new",
                    "notes": "Updated notes",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", updatedAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/assignments/" + existingAssignment.getId())
                        .file(assignmentDtoPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testUpdateAssignment_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();

        String updatedAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-01-01",
                    "conditionOut": "Like new",
                    "notes": "Updated notes",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", updatedAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/assignments/" + existingAssignment.getId())
                        .file(assignmentDtoPart))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testUpdateAssignment_withImageFileNotInKeepIds_shouldDeleteImageFromCloudinary() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User, List.of(new SimpleGrantedAuthority("USER")), "github"
        );

        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();
        assignmentFileRepository.save(new AssignmentFileModel(
                null, existingAssignment, "https://cloudinary.test/old-image.png", "image/png", LocalDateTime.now()
        ));
        // Force a real reload so the controller sees a Hibernate-managed files collection
        // instead of the stale, already-loaded (empty) one cached on this persistence context.
        entityManager.flush();
        entityManager.clear();

        // assignmentDTO.files() omitted -> keepIds empty -> the existing image file must be removed
        String updatedAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-01-01",
                    "conditionOut": "Like new",
                    "notes": "Updated notes",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", updatedAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/assignments/" + existingAssignment.getId())
                        .file(assignmentDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk());

        verify(cloudinaryService).deleteImage("https://cloudinary.test/old-image.png");
        verify(cloudinaryService, never()).deleteFile(anyString());
        assertTrue(assignmentFileRepository.findAll().isEmpty());
    }

    @Test
    void testUpdateAssignment_withNonImageFileNotInKeepIds_shouldDeleteFileFromCloudinary() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User, List.of(new SimpleGrantedAuthority("USER")), "github"
        );

        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();
        assignmentFileRepository.save(new AssignmentFileModel(
                null, existingAssignment, "https://cloudinary.test/old-doc.pdf", "application/pdf", LocalDateTime.now()
        ));
        entityManager.flush();
        entityManager.clear();

        // assignmentDTO.files() omitted -> keepIds empty -> the existing document file must be removed
        String updatedAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-01-01",
                    "conditionOut": "Like new",
                    "notes": "Updated notes",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", updatedAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/assignments/" + existingAssignment.getId())
                        .file(assignmentDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk());

        verify(cloudinaryService).deleteFile("https://cloudinary.test/old-doc.pdf");
        verify(cloudinaryService, never()).deleteImage(anyString());
        assertTrue(assignmentFileRepository.findAll().isEmpty());
    }

    @Test
    void testUpdateAssignment_withFileKeptInKeepIds_shouldNotDeleteFromCloudinary() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User, List.of(new SimpleGrantedAuthority("USER")), "github"
        );

        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();
        AssignmentFileModel keptFile = assignmentFileRepository.save(new AssignmentFileModel(
                null, existingAssignment, "https://cloudinary.test/kept-image.png", "image/png", LocalDateTime.now()
        ));
        entityManager.flush();
        entityManager.clear();

        // assignmentDTO.files() references the existing file's id -> keepIds contains it -> must NOT be deleted
        String updatedAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-01-01",
                    "conditionOut": "Like new",
                    "notes": "Updated notes",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false,
                    "files": [
                        { "id": "%s", "fileUrl": "https://cloudinary.test/kept-image.png", "fileType": "image/png", "uploadedAt": "2024-01-01T10:00:00" }
                    ]
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId(), keptFile.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", updatedAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/assignments/" + existingAssignment.getId())
                        .file(assignmentDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk());

        verify(cloudinaryService, never()).deleteImage(anyString());
        verify(cloudinaryService, never()).deleteFile(anyString());
        assertTrue(assignmentFileRepository.findById(keptFile.getId()).isPresent());
    }

    @Test
    void testUpdateAssignment_withNewImageFile_shouldUploadImageAndSaveFile() throws Exception {
        when(cloudinaryService.uploadImage(any())).thenReturn("https://cloudinary.test/updated-image.png");

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User, List.of(new SimpleGrantedAuthority("USER")), "github"
        );

        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();

        String updatedAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-01-01",
                    "conditionOut": "Like new",
                    "notes": "Updated notes",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", updatedAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile imageFilePart = new MockMultipartFile(
                "files", "new-photo.png", "image/png", "image-bytes".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/assignments/" + existingAssignment.getId())
                        .file(assignmentDtoPart)
                        .file(imageFilePart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk());

        verify(cloudinaryService).uploadImage(any());
        verify(cloudinaryService, never()).uploadFile(any());

        List<AssignmentFileModel> savedFiles = assignmentFileRepository.findAll();
        assertEquals(1, savedFiles.size());
        assertEquals("https://cloudinary.test/updated-image.png", savedFiles.getFirst().getFileUrl());
    }

    @Test
    void testUpdateAssignment_withNewNonImageFile_shouldUploadFileAndSaveFile() throws Exception {
        when(cloudinaryService.uploadFile(any())).thenReturn("https://cloudinary.test/updated-doc.pdf");

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User, List.of(new SimpleGrantedAuthority("USER")), "github"
        );

        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();

        String updatedAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-01-01",
                    "conditionOut": "Like new",
                    "notes": "Updated notes",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", updatedAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile docFilePart = new MockMultipartFile(
                "files", "new-protocol.pdf", "application/pdf", "pdf-bytes".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/assignments/" + existingAssignment.getId())
                        .file(assignmentDtoPart)
                        .file(docFilePart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk());

        verify(cloudinaryService).uploadFile(any());
        verify(cloudinaryService, never()).uploadImage(any());
    }

    @Test
    void testUpdateAssignment_withEmptyFilePart_shouldNotUploadAnything() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User, List.of(new SimpleGrantedAuthority("USER")), "github"
        );

        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();

        String updatedAssignmentJson = """
                {
                    "device": { "id": "%s", "defective": false },
                    "employee": { "id": "%s", "active": true },
                    "assignedDate": "2024-01-01",
                    "conditionOut": "Like new",
                    "notes": "Updated notes",
                    "copyHandedToEmployee": true,
                    "copyFiledInPersonnelFile": false
                }
                """.formatted(deviceModel1.getId(), employeeModel1.getId());

        MockMultipartFile assignmentDtoPart = new MockMultipartFile(
                "assignmentDTO", "", "application/json", updatedAssignmentJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile emptyFilePart = new MockMultipartFile(
                "files", "empty.txt", "text/plain", new byte[0]
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/assignments/" + existingAssignment.getId())
                        .file(assignmentDtoPart)
                        .file(emptyFilePart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk());

        verify(cloudinaryService, never()).uploadImage(any());
        verify(cloudinaryService, never()).uploadFile(any());
    }

    @Test
    void testDeleteAssignment_shouldReturnNoContent() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/assignments/" + existingAssignment.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());

        assertFalse(assignmentRepository.existsById(existingAssignment.getId()));
    }

    @Test
    void testDeleteAssignment_unauthenticated_shouldReturnUnauthorized() throws Exception {
        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/assignments/" + existingAssignment.getId()))
                .andExpect(status().isUnauthorized());

        assertTrue(assignmentRepository.existsById(existingAssignment.getId()));
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testDeleteAssignment_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        AssignmentModel existingAssignment = assignmentRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/assignments/" + existingAssignment.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));

        assertTrue(assignmentRepository.existsById(existingAssignment.getId()));
    }
}