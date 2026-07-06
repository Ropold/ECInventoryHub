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
import ropold.backend.model.Department;
import ropold.backend.model.EmployeeModel;
import ropold.backend.repository.EmployeeRepository;
import ropold.backend.service.CloudinaryService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
class EmployeeControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        employeeRepository.deleteAll();

        EmployeeModel employeeModel1 = new EmployeeModel(
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
        );

        EmployeeModel employeeModel2 = new EmployeeModel(
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
        );

        employeeRepository.saveAll(List.of(employeeModel1, employeeModel2));
    }

    @Test
    void testGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Max Mustermann"))
                .andExpect(jsonPath("$[1].name").value("Erika Musterfrau"));
    }

    @Test
    void testGetEmployeeById() throws Exception {
        EmployeeModel savedEmployee = employeeRepository.findAll().getFirst();

        mockMvc.perform(get("/api/employees/" + savedEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Max Mustermann"))
                .andExpect(jsonPath("$.personnelNumber").value("P-1001"));
    }

    @Test
    void testGetEmployeeById_notFound() throws Exception {
        mockMvc.perform(get("/api/employees/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddEmployee_shouldReturnCreated() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        employeeRepository.deleteAll();

        String newEmployeeJson = """
                {
                    "personnelNumber": "P-3003",
                    "name": "New Employee",
                    "email": "new.employee@example.com",
                    "phone": "+49 170 1112223",
                    "address": "Neue Strasse 3, 11111 Neustadt",
                    "department": "MARKETING",
                    "active": true,
                    "notes": "None"
                }
                """;

        MockMultipartFile employeeDtoPart = new MockMultipartFile(
                "employeeDTO", "", "application/json", newEmployeeJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/employees")
                        .file(employeeDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Employee"))
                .andExpect(jsonPath("$.personnelNumber").value("P-3003"));

        List<EmployeeModel> allEmployees = employeeRepository.findAll();
        assertEquals(1, allEmployees.size());
    }

    @Test
    void testAddEmployee_unauthenticated_shouldReturnUnauthorized() throws Exception {
        String newEmployeeJson = """
                {
                    "personnelNumber": "P-3003",
                    "name": "New Employee",
                    "email": "new.employee@example.com",
                    "phone": "+49 170 1112223",
                    "address": "Neue Strasse 3, 11111 Neustadt",
                    "department": "MARKETING",
                    "active": true,
                    "notes": "None"
                }
                """;

        MockMultipartFile employeeDtoPart = new MockMultipartFile(
                "employeeDTO", "", "application/json", newEmployeeJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/employees")
                        .file(employeeDtoPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testAddEmployee_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        String newEmployeeJson = """
                {
                    "personnelNumber": "P-3003",
                    "name": "New Employee",
                    "email": "new.employee@example.com",
                    "phone": "+49 170 1112223",
                    "address": "Neue Strasse 3, 11111 Neustadt",
                    "department": "MARKETING",
                    "active": true,
                    "notes": "None"
                }
                """;

        MockMultipartFile employeeDtoPart = new MockMultipartFile(
                "employeeDTO", "", "application/json", newEmployeeJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/employees")
                        .file(employeeDtoPart))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testAddEmployee_withImage_shouldUploadImage() throws Exception {
        when(cloudinaryService.uploadImage(any())).thenReturn("https://cloudinary.test/new-employee.png");

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User, List.of(new SimpleGrantedAuthority("USER")), "github"
        );

        employeeRepository.deleteAll();

        String newEmployeeJson = """
                {
                    "personnelNumber": "P-3003",
                    "name": "New Employee",
                    "email": "new.employee@example.com",
                    "phone": "+49 170 1112223",
                    "address": "Neue Strasse 3, 11111 Neustadt",
                    "department": "MARKETING",
                    "active": true,
                    "notes": "None"
                }
                """;

        MockMultipartFile employeeDtoPart = new MockMultipartFile(
                "employeeDTO", "", "application/json", newEmployeeJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile imagePart = new MockMultipartFile(
                "image", "photo.png", "image/png", "image-bytes".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/employees")
                        .file(employeeDtoPart)
                        .file(imagePart)
                        .with(authentication(authToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrl").value("https://cloudinary.test/new-employee.png"));

        verify(cloudinaryService).uploadImage(any());
    }

    @Test
    void testAddEmployee_withEmptyImagePart_shouldNotUploadImage() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User, List.of(new SimpleGrantedAuthority("USER")), "github"
        );

        employeeRepository.deleteAll();

        String newEmployeeJson = """
                {
                    "personnelNumber": "P-3003",
                    "name": "New Employee",
                    "email": "new.employee@example.com",
                    "phone": "+49 170 1112223",
                    "address": "Neue Strasse 3, 11111 Neustadt",
                    "department": "MARKETING",
                    "active": true,
                    "notes": "None"
                }
                """;

        MockMultipartFile employeeDtoPart = new MockMultipartFile(
                "employeeDTO", "", "application/json", newEmployeeJson.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile emptyImagePart = new MockMultipartFile(
                "image", "empty.png", "image/png", new byte[0]
        );

        mockMvc.perform(multipart("/api/employees")
                        .file(employeeDtoPart)
                        .file(emptyImagePart)
                        .with(authentication(authToken)))
                .andExpect(status().isCreated());

        verify(cloudinaryService, never()).uploadImage(any());
        assertNull(employeeRepository.findAll().getFirst().getImageUrl());
    }

    @Test
    void testUpdateEmployee_shouldReturnUpdated() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        EmployeeModel existingEmployee = employeeRepository.findAll().getFirst();

        // imageUrl matches the existing one so ImageUploadUtil keeps it unchanged
        // and never calls out to the real Cloudinary API during this test.
        String updatedEmployeeJson = """
                {
                    "personnelNumber": "P-1001",
                    "name": "Max Mustermann Updated",
                    "email": "max.mustermann@example.com",
                    "phone": "+49 170 1234567",
                    "address": "Musterstrasse 1, 12345 Musterstadt",
                    "department": "MANAGEMENT",
                    "active": false,
                    "notes": "Updated notes",
                    "imageUrl": "https://example.com/employee1.jpg"
                }
                """;

        MockMultipartFile employeeDtoPart = new MockMultipartFile(
                "employeeDTO", "", "application/json", updatedEmployeeJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/employees/" + existingEmployee.getId())
                        .file(employeeDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Max Mustermann Updated"))
                .andExpect(jsonPath("$.department").value("MANAGEMENT"))
                .andExpect(jsonPath("$.active").value(false));

        EmployeeModel updated = employeeRepository.findById(existingEmployee.getId()).orElseThrow();
        assertEquals("Max Mustermann Updated", updated.getName());
        assertFalse(updated.isActive());
    }

    @Test
    void testUpdateEmployee_unauthenticated_shouldReturnUnauthorized() throws Exception {
        EmployeeModel existingEmployee = employeeRepository.findAll().getFirst();

        String updatedEmployeeJson = """
                {
                    "personnelNumber": "P-1001",
                    "name": "Max Mustermann Updated",
                    "email": "max.mustermann@example.com",
                    "phone": "+49 170 1234567",
                    "address": "Musterstrasse 1, 12345 Musterstadt",
                    "department": "MANAGEMENT",
                    "active": false,
                    "notes": "Updated notes",
                    "imageUrl": "https://example.com/employee1.jpg"
                }
                """;

        MockMultipartFile employeeDtoPart = new MockMultipartFile(
                "employeeDTO", "", "application/json", updatedEmployeeJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/employees/" + existingEmployee.getId())
                        .file(employeeDtoPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testUpdateEmployee_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        EmployeeModel existingEmployee = employeeRepository.findAll().getFirst();

        String updatedEmployeeJson = """
                {
                    "personnelNumber": "P-1001",
                    "name": "Max Mustermann Updated",
                    "email": "max.mustermann@example.com",
                    "phone": "+49 170 1234567",
                    "address": "Musterstrasse 1, 12345 Musterstadt",
                    "department": "MANAGEMENT",
                    "active": false,
                    "notes": "Updated notes",
                    "imageUrl": "https://example.com/employee1.jpg"
                }
                """;

        MockMultipartFile employeeDtoPart = new MockMultipartFile(
                "employeeDTO", "", "application/json", updatedEmployeeJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/employees/" + existingEmployee.getId())
                        .file(employeeDtoPart))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testDeleteEmployee_shouldReturnNoContent() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        EmployeeModel existingEmployee = employeeRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/employees/" + existingEmployee.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());

        assertFalse(employeeRepository.existsById(existingEmployee.getId()));
    }

    @Test
    void testDeleteEmployee_unauthenticated_shouldReturnUnauthorized() throws Exception {
        EmployeeModel existingEmployee = employeeRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/employees/" + existingEmployee.getId()))
                .andExpect(status().isUnauthorized());

        assertTrue(employeeRepository.existsById(existingEmployee.getId()));
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testDeleteEmployee_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        EmployeeModel existingEmployee = employeeRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/employees/" + existingEmployee.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));

        assertTrue(employeeRepository.existsById(existingEmployee.getId()));
    }
}