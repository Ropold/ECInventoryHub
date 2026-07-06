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
import ropold.backend.model.DeviceModel;
import ropold.backend.model.DeviceStatus;
import ropold.backend.model.DeviceType;
import ropold.backend.model.LocationModel;
import ropold.backend.repository.AssignmentRepository;
import ropold.backend.repository.DeviceRepository;
import ropold.backend.repository.LocationRepository;

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
class DeviceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Delete in correct order to respect foreign key constraints
        assignmentRepository.deleteAll();
        deviceRepository.deleteAll();
        locationRepository.deleteAll();

        LocationModel locationModel1 = new LocationModel(
                null,
                "Location One",
                "Musterstrasse 1, 12345 Musterstadt",
                "+49 170 1234567",
                "location.one@example.com",
                "Notes for location one",
                "https://example.com/location1.jpg"
        );

        LocationModel savedLocation = locationRepository.save(locationModel1);

        DeviceModel deviceModel1 = new DeviceModel(
                null,
                DeviceType.LAPTOP,
                "Dell",
                "Latitude 5420",
                "SN-1001",
                "INV-1001",
                LocalDate.of(2023, 1, 15),
                DeviceStatus.ASSIGNED,
                false,
                savedLocation,
                "Notes for device one",
                new ArrayList<>()
        );

        DeviceModel deviceModel2 = new DeviceModel(
                null,
                DeviceType.PHONE,
                "Apple",
                "iPhone 14",
                "SN-2002",
                "INV-2002",
                LocalDate.of(2023, 6, 1),
                DeviceStatus.AVAILABLE,
                false,
                null,
                "Notes for device two",
                new ArrayList<>()
        );

        deviceRepository.saveAll(List.of(deviceModel1, deviceModel2));
    }

    @Test
    void testGetAllDevices() throws Exception {
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].manufacturer").value("Dell"))
                .andExpect(jsonPath("$[1].manufacturer").value("Apple"));
    }

    @Test
    void testGetDeviceById() throws Exception {
        DeviceModel savedDevice = deviceRepository.findAll().getFirst();

        mockMvc.perform(get("/api/devices/" + savedDevice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer").value("Dell"))
                .andExpect(jsonPath("$.serialNumber").value("SN-1001"));
    }

    @Test
    void testGetDeviceById_notFound() throws Exception {
        mockMvc.perform(get("/api/devices/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddDevice_shouldReturnCreated() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        assignmentRepository.deleteAll();
        deviceRepository.deleteAll();

        String newDeviceJson = """
                {
                    "type": "MONITOR",
                    "manufacturer": "Samsung",
                    "modelName": "Odyssey G7",
                    "serialNumber": "SN-3003",
                    "inventoryNumber": "INV-3003",
                    "purchaseDate": "2024-02-01",
                    "status": "AVAILABLE",
                    "defective": false,
                    "notes": "Notes for new device"
                }
                """;

        MockMultipartFile deviceDtoPart = new MockMultipartFile(
                "deviceDTO", "", "application/json", newDeviceJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/devices")
                        .file(deviceDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.manufacturer").value("Samsung"))
                .andExpect(jsonPath("$.serialNumber").value("SN-3003"));

        List<DeviceModel> allDevices = deviceRepository.findAll();
        assertEquals(1, allDevices.size());
    }

    @Test
    void testAddDevice_unauthenticated_shouldReturnUnauthorized() throws Exception {
        String newDeviceJson = """
                {
                    "type": "MONITOR",
                    "manufacturer": "Samsung",
                    "modelName": "Odyssey G7",
                    "serialNumber": "SN-3003",
                    "inventoryNumber": "INV-3003",
                    "purchaseDate": "2024-02-01",
                    "status": "AVAILABLE",
                    "defective": false,
                    "notes": "Notes for new device"
                }
                """;

        MockMultipartFile deviceDtoPart = new MockMultipartFile(
                "deviceDTO", "", "application/json", newDeviceJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/devices")
                        .file(deviceDtoPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testAddDevice_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        String newDeviceJson = """
                {
                    "type": "MONITOR",
                    "manufacturer": "Samsung",
                    "modelName": "Odyssey G7",
                    "serialNumber": "SN-3003",
                    "inventoryNumber": "INV-3003",
                    "purchaseDate": "2024-02-01",
                    "status": "AVAILABLE",
                    "defective": false,
                    "notes": "Notes for new device"
                }
                """;

        MockMultipartFile deviceDtoPart = new MockMultipartFile(
                "deviceDTO", "", "application/json", newDeviceJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/devices")
                        .file(deviceDtoPart))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testUpdateDevice_shouldReturnUpdated() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        DeviceModel existingDevice = deviceRepository.findAll().getFirst();

        // No "files" part sent and deviceDTO.files() omitted -> keepIds is empty,
        // but the existing device has no files persisted, so no Cloudinary cleanup call happens.
        String updatedDeviceJson = """
                {
                    "type": "LAPTOP",
                    "manufacturer": "Dell",
                    "modelName": "Latitude 5430",
                    "serialNumber": "SN-1001",
                    "inventoryNumber": "INV-1001",
                    "purchaseDate": "2023-01-15",
                    "status": "IN_REPAIR",
                    "defective": true,
                    "notes": "Updated notes"
                }
                """;

        MockMultipartFile deviceDtoPart = new MockMultipartFile(
                "deviceDTO", "", "application/json", updatedDeviceJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/devices/" + existingDevice.getId())
                        .file(deviceDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName").value("Latitude 5430"))
                .andExpect(jsonPath("$.status").value("IN_REPAIR"))
                .andExpect(jsonPath("$.defective").value(true));

        DeviceModel updated = deviceRepository.findById(existingDevice.getId()).orElseThrow();
        assertEquals("Latitude 5430", updated.getModelName());
        assertEquals(DeviceStatus.IN_REPAIR, updated.getStatus());
    }

    @Test
    void testUpdateDevice_unauthenticated_shouldReturnUnauthorized() throws Exception {
        DeviceModel existingDevice = deviceRepository.findAll().getFirst();

        String updatedDeviceJson = """
                {
                    "type": "LAPTOP",
                    "manufacturer": "Dell",
                    "modelName": "Latitude 5430",
                    "serialNumber": "SN-1001",
                    "inventoryNumber": "INV-1001",
                    "purchaseDate": "2023-01-15",
                    "status": "IN_REPAIR",
                    "defective": true,
                    "notes": "Updated notes"
                }
                """;

        MockMultipartFile deviceDtoPart = new MockMultipartFile(
                "deviceDTO", "", "application/json", updatedDeviceJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/devices/" + existingDevice.getId())
                        .file(deviceDtoPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testUpdateDevice_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        DeviceModel existingDevice = deviceRepository.findAll().getFirst();

        String updatedDeviceJson = """
                {
                    "type": "LAPTOP",
                    "manufacturer": "Dell",
                    "modelName": "Latitude 5430",
                    "serialNumber": "SN-1001",
                    "inventoryNumber": "INV-1001",
                    "purchaseDate": "2023-01-15",
                    "status": "IN_REPAIR",
                    "defective": true,
                    "notes": "Updated notes"
                }
                """;

        MockMultipartFile deviceDtoPart = new MockMultipartFile(
                "deviceDTO", "", "application/json", updatedDeviceJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/devices/" + existingDevice.getId())
                        .file(deviceDtoPart))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testDeleteDevice_shouldReturnNoContent() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        DeviceModel existingDevice = deviceRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/devices/" + existingDevice.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());

        assertFalse(deviceRepository.existsById(existingDevice.getId()));
    }

    @Test
    void testDeleteDevice_unauthenticated_shouldReturnUnauthorized() throws Exception {
        DeviceModel existingDevice = deviceRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/devices/" + existingDevice.getId()))
                .andExpect(status().isUnauthorized());

        assertTrue(deviceRepository.existsById(existingDevice.getId()));
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testDeleteDevice_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        DeviceModel existingDevice = deviceRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/devices/" + existingDevice.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));

        assertTrue(deviceRepository.existsById(existingDevice.getId()));
    }
}