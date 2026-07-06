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
import ropold.backend.model.LocationModel;
import ropold.backend.repository.LocationRepository;

import java.nio.charset.StandardCharsets;
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
class LocationControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

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

        LocationModel locationModel2 = new LocationModel(
                null,
                "Location Two",
                "Beispielweg 2, 54321 Beispielstadt",
                "+49 170 7654321",
                "location.two@example.com",
                "Notes for location two",
                "https://example.com/location2.jpg"
        );

        locationRepository.saveAll(List.of(locationModel1, locationModel2));
    }

    @Test
    void testGetAllLocations() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Location One"))
                .andExpect(jsonPath("$[1].name").value("Location Two"));
    }

    @Test
    void testGetLocationById() throws Exception {
        LocationModel savedLocation = locationRepository.findAll().getFirst();

        mockMvc.perform(get("/api/locations/" + savedLocation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Location One"))
                .andExpect(jsonPath("$.email").value("location.one@example.com"));
    }

    @Test
    void testGetLocationById_notFound() throws Exception {
        mockMvc.perform(get("/api/locations/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddLocation_shouldReturnCreated() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        locationRepository.deleteAll();

        String newLocationJson = """
                {
                    "name": "New Location",
                    "address": "Neue Strasse 3, 11111 Neustadt",
                    "phone": "+49 170 1112223",
                    "email": "new.location@example.com",
                    "notes": "None"
                }
                """;

        MockMultipartFile locationDtoPart = new MockMultipartFile(
                "locationDTO", "", "application/json", newLocationJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/locations")
                        .file(locationDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Location"))
                .andExpect(jsonPath("$.email").value("new.location@example.com"));

        List<LocationModel> allLocations = locationRepository.findAll();
        assertEquals(1, allLocations.size());
    }

    @Test
    void testAddLocation_unauthenticated_shouldReturnUnauthorized() throws Exception {
        String newLocationJson = """
                {
                    "name": "New Location",
                    "address": "Neue Strasse 3, 11111 Neustadt",
                    "phone": "+49 170 1112223",
                    "email": "new.location@example.com",
                    "notes": "None"
                }
                """;

        MockMultipartFile locationDtoPart = new MockMultipartFile(
                "locationDTO", "", "application/json", newLocationJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/locations")
                        .file(locationDtoPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testAddLocation_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        String newLocationJson = """
                {
                    "name": "New Location",
                    "address": "Neue Strasse 3, 11111 Neustadt",
                    "phone": "+49 170 1112223",
                    "email": "new.location@example.com",
                    "notes": "None"
                }
                """;

        MockMultipartFile locationDtoPart = new MockMultipartFile(
                "locationDTO", "", "application/json", newLocationJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/locations")
                        .file(locationDtoPart))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testUpdateLocation_shouldReturnUpdated() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        LocationModel existingLocation = locationRepository.findAll().getFirst();

        // imageUrl matches the existing one so ImageUploadUtil keeps it unchanged
        // and never calls out to the real Cloudinary API during this test.
        String updatedLocationJson = """
                {
                    "name": "Location One Updated",
                    "address": "Musterstrasse 1, 12345 Musterstadt",
                    "phone": "+49 170 1234567",
                    "email": "location.one@example.com",
                    "notes": "Updated notes",
                    "imageUrl": "https://example.com/location1.jpg"
                }
                """;

        MockMultipartFile locationDtoPart = new MockMultipartFile(
                "locationDTO", "", "application/json", updatedLocationJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/locations/" + existingLocation.getId())
                        .file(locationDtoPart)
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Location One Updated"))
                .andExpect(jsonPath("$.notes").value("Updated notes"));

        LocationModel updated = locationRepository.findById(existingLocation.getId()).orElseThrow();
        assertEquals("Location One Updated", updated.getName());
    }

    @Test
    void testUpdateLocation_unauthenticated_shouldReturnUnauthorized() throws Exception {
        LocationModel existingLocation = locationRepository.findAll().getFirst();

        String updatedLocationJson = """
                {
                    "name": "Location One Updated",
                    "address": "Musterstrasse 1, 12345 Musterstadt",
                    "phone": "+49 170 1234567",
                    "email": "location.one@example.com",
                    "notes": "Updated notes",
                    "imageUrl": "https://example.com/location1.jpg"
                }
                """;

        MockMultipartFile locationDtoPart = new MockMultipartFile(
                "locationDTO", "", "application/json", updatedLocationJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/locations/" + existingLocation.getId())
                        .file(locationDtoPart))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testUpdateLocation_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        LocationModel existingLocation = locationRepository.findAll().getFirst();

        String updatedLocationJson = """
                {
                    "name": "Location One Updated",
                    "address": "Musterstrasse 1, 12345 Musterstadt",
                    "phone": "+49 170 1234567",
                    "email": "location.one@example.com",
                    "notes": "Updated notes",
                    "imageUrl": "https://example.com/location1.jpg"
                }
                """;

        MockMultipartFile locationDtoPart = new MockMultipartFile(
                "locationDTO", "", "application/json", updatedLocationJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/locations/" + existingLocation.getId())
                        .file(locationDtoPart))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testDeleteLocation_shouldReturnNoContent() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("test-user");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("USER")),
                "github"
        );

        LocationModel existingLocation = locationRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/locations/" + existingLocation.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());

        assertFalse(locationRepository.existsById(existingLocation.getId()));
    }

    @Test
    void testDeleteLocation_unauthenticated_shouldReturnUnauthorized() throws Exception {
        LocationModel existingLocation = locationRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/locations/" + existingLocation.getId()))
                .andExpect(status().isUnauthorized());

        assertTrue(locationRepository.existsById(existingLocation.getId()));
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    void testDeleteLocation_withoutOAuth2Principal_shouldReturnInternalServerError() throws Exception {
        LocationModel existingLocation = locationRepository.findAll().getFirst();

        mockMvc.perform(delete("/api/locations/" + existingLocation.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));

        assertTrue(locationRepository.existsById(existingLocation.getId()));
    }
}