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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ropold.backend.model.AssignmentModel;
import ropold.backend.model.Department;
import ropold.backend.model.DeviceModel;
import ropold.backend.model.DeviceStatus;
import ropold.backend.model.DeviceType;
import ropold.backend.model.EmployeeModel;
import ropold.backend.repository.AssignmentRepository;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.EmployeeRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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