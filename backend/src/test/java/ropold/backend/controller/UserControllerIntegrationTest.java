package ropold.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ropold.backend.model.Role;
import ropold.backend.model.UserModel;
import ropold.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @Test
    void testGetMe() throws Exception {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getName()).thenReturn("userName1");
        when(mockOAuth2User.getAttribute("id")).thenReturn("githubId1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockOAuth2User,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"  // registrationId für GitHub
        );

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("userName1"));
    }

    @Test
    void testGetMe_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("anonymousUser"));
    }

    @Test
    void testGetUserDetails_withLoggedInUser() throws Exception {
        // Erstellen eines Mock OAuth2User
        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getAttribute("id")).thenReturn("githubId1");
        when(mockUser.getAttribute("login")).thenReturn("userName1");
        when(mockUser.getAttribute("name")).thenReturn("Test User 1");
        when(mockUser.getAttribute("avatar_url")).thenReturn("https://avatars.githubusercontent.com/u/123456");
        when(mockUser.getAttribute("html_url")).thenReturn("https://github.com/userName1");
        when(mockUser.getAttribute("role")).thenReturn("USER");
        when(mockUser.getAttribute("preferred_language")).thenReturn("de");

        // Verwende OAuth2AuthenticationToken statt UsernamePasswordAuthenticationToken

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockUser,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/details")
                        .with(authentication(authToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.githubId").value("githubId1"))
                .andExpect(jsonPath("$.username").value("userName1"))
                .andExpect(jsonPath("$.name").value("Test User 1"))
                .andExpect(jsonPath("$.avatarUrl").value("https://avatars.githubusercontent.com/u/123456"))
                .andExpect(jsonPath("$.githubUrl").value("https://github.com/userName1"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.preferredLanguage").value("de"));
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
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUserDetails_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    void testGetUserDetails_withUserWithNullName() throws Exception {
        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getAttribute("id")).thenReturn("githubId1");
        when(mockUser.getAttribute("login")).thenReturn("userName1");
        when(mockUser.getAttribute("name")).thenReturn(null);  // null name should fallback to username
        when(mockUser.getAttribute("avatar_url")).thenReturn("https://avatars.githubusercontent.com/u/123456");
        when(mockUser.getAttribute("html_url")).thenReturn("https://github.com/userName1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockUser,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/details")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.githubId").value("githubId1"));
    }

    @Test
    void testGetUserDetails_newUser_currentlyThrowsClassCastExceptionInCreateUser() throws Exception {
        // KNOWN BUG (reported separately, not fixed here): createUserFromAuthentication() does
        // `String.valueOf(authentication.getAttribute("id"))` with the generic getAttribute(...) call
        // inlined as the argument. javac resolves this to the String.valueOf(char[]) overload instead
        // of String.valueOf(Object), so this always throws a ClassCastException at runtime, regardless
        // of the actual attribute type. The outer catch(Exception) swallows it and returns the generic
        // "temporarily unavailable" error, so new-user auto-creation via this endpoint is broken today.
        // This test documents the current (buggy) behavior and covers the userOpt.isEmpty()==true branch;
        // the deeper branches inside createUserFromAuthentication cannot be covered until the bug is fixed.
        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getAttribute("id")).thenReturn("githubIdNew");
        when(mockUser.getAttribute("login")).thenReturn("newUserName");
        when(mockUser.getAttribute("name")).thenReturn("Brand New User");
        when(mockUser.getAttribute("avatar_url")).thenReturn("https://avatars.githubusercontent.com/u/999");
        when(mockUser.getAttribute("html_url")).thenReturn("https://github.com/newUserName");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockUser,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/details")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("User data temporarily unavailable, please refresh"));

        assertThat(userRepository.findByGithubId("githubIdNew")).isEmpty();
    }

    @Test
    void testGetUserDetails_withNullAvatarAndGithubUrl_shouldReturnEmptyStrings() throws Exception {
        testUser.setAvatarUrl(null);
        testUser.setGithubUrl(null);
        userRepository.save(testUser);

        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getAttribute("id")).thenReturn("githubId1");

        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                mockUser,
                List.of(new SimpleGrantedAuthority("OIDC_USER")),
                "github"
        );

        mockMvc.perform(get("/api/users/me/details")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value(""))
                .andExpect(jsonPath("$.githubUrl").value(""));
    }

    @Test
    void testGetUserDetails_withException() throws Exception {
        OAuth2User mockUser = mock(OAuth2User.class);
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