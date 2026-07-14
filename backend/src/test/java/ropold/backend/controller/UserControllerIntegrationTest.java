package ropold.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import ropold.backend.model.Role;
import ropold.backend.model.UserModel;
import ropold.backend.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @MockitoBean
    private RestTemplate restTemplate;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        userRepository.deleteAll();

        UserModel user1 = new UserModel();
        user1.setGithubId("githubId1");
        user1.setUsername("userName1");
        user1.setName("Test User 1");
        user1.setAvatarUrl("https://avatars.githubusercontent.com/u/123456");
        user1.setGithubUrl("https://github.com/userName1");
        user1.setRole(Role.USER);
        user1.setPreferredLanguage("de");
        user1.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));
        user1.setLastLoginAt(LocalDateTime.of(2024, 1, 1, 12, 30));

        testUser = userRepository.save(user1);
    }

    private void mockAuthorizedClientAndGithubResponse(Map<String, Object> githubResponseBody) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(clientRegistration, "githubId1", accessToken);

        when(authorizedClientService.loadAuthorizedClient(eq("github"), anyString())).thenReturn(authorizedClient);

        when(restTemplate.exchange(
                eq("https://api.github.com/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(githubResponseBody));
    }

    @Test
    void testGetMe() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn("githubId1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"  // registrationId für GitHub
        );

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("githubId1"));
    }

    @Test
    void testGetMe_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("anonymousUser"));
    }

    @Test
    void testGetUserDetails_withLoggedInUser() throws Exception {
        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getName()).thenReturn("userName1");
        when(mockUser.getAttribute("id")).thenReturn("githubId1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockUser,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockAuthorizedClientAndGithubResponse(Map.of(
                "login", "userName1",
                "id", 123456,
                "bio", "Test bio",
                "followers", 42,
                "public_repos", 12,
                "html_url", "https://github.com/userName1"
        ));

        mockMvc.perform(get("/api/users/me/details")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("userName1"))
                .andExpect(jsonPath("$.bio").value("Test bio"))
                .andExpect(jsonPath("$.followers").value(42))
                .andExpect(jsonPath("$.public_repos").value(12))
                .andExpect(jsonPath("$.html_url").value("https://github.com/userName1"));

        UserModel updatedUser = userRepository.findByGithubId("githubId1").orElseThrow();
        assertThat(updatedUser.getLastLoginAt()).isAfter(LocalDateTime.of(2024, 1, 1, 12, 30));
    }

    @Test
    void testGetUserDetails_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testGetUserDetails_newUser_createsUserWithNullNameFallback() throws Exception {
        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getName()).thenReturn("newUserName");
        when(mockUser.getAttribute("id")).thenReturn("githubIdNew");
        when(mockUser.getAttribute("login")).thenReturn("newUserName");
        when(mockUser.getAttribute("name")).thenReturn(null); // null name should fallback to username
        when(mockUser.getAttribute("avatar_url")).thenReturn("https://avatars.githubusercontent.com/u/999");
        when(mockUser.getAttribute("html_url")).thenReturn("https://github.com/newUserName");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockUser,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockAuthorizedClientAndGithubResponse(Map.of(
                "login", "newUserName",
                "id", 999
        ));

        mockMvc.perform(get("/api/users/me/details")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("newUserName"));

        UserModel newUser = userRepository.findByGithubId("githubIdNew").orElseThrow();
        assertThat(newUser.getUsername()).isEqualTo("newUserName");
        assertThat(newUser.getName()).isEqualTo("newUserName"); // fallback to username since name was null
        assertThat(newUser.getRole()).isEqualTo(Role.VIEWER);
    }

    @Test
    void testGetUserDetails_withException() throws Exception {
        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getName()).thenReturn("userName1");
        when(mockUser.getAttribute("id")).thenThrow(new RuntimeException("Test exception"));

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockUser,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/details")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("User data temporarily unavailable, please refresh"));
    }

    @Test
    void testGetMyRole_withLoggedInUser() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn("githubId1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/role")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("USER"));
    }

    @Test
    void testGetMyRole_reflectsAdminRole() throws Exception {
        testUser.setRole(Role.ADMIN);
        userRepository.save(testUser);

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn("githubId1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/role")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("ADMIN"));
    }

    @Test
    void testGetMyRole_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me/role"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testGetMyRole_withNullGithubId() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn(null);

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/role")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("GitHub ID not found"));
    }

    @Test
    void testGetMyRole_unknownGithubId_throwsUserNotFound() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn("unknownGithubId");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/role")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testGetPreferredLanguage_withLoggedInUser() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn("githubId1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/language")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("de"));
    }

    @Test
    void testGetPreferredLanguage_reflectsUpdatedLanguage() throws Exception {
        testUser.setPreferredLanguage("en");
        userRepository.save(testUser);

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn("githubId1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/language")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("en"));
    }

    @Test
    void testGetPreferredLanguage_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me/language"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testGetPreferredLanguage_withNullGithubId() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn(null);

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/language")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("GitHub ID not found"));
    }

    @Test
    void testGetPreferredLanguage_unknownGithubId_throwsUserNotFound() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn("unknownGithubId");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/language")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testSetPreferredLanguage_withLoggedInUser() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn("githubId1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER"), new SimpleGrantedAuthority("USER")),
                "github"
        );

        mockMvc.perform(post("/api/users/me/language/en")
                        .with(authentication(authToken)))
                .andExpect(status().isOk());

        UserModel updatedUser = userRepository.findByGithubId("githubId1").orElseThrow();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getPreferredLanguage()).isEqualTo("en");
    }

    @Test
    void testSetPreferredLanguage_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/users/me/language/en"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testSetPreferredLanguage_withNullGithubId() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn(null);

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER"), new SimpleGrantedAuthority("USER")),
                "github"
        );

        mockMvc.perform(post("/api/users/me/language/en")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("GitHub ID not found"));
    }

}