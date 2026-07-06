package ropold.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.core.user.OAuth2User;
import ropold.backend.model.Role;
import ropold.backend.model.UserModel;
import ropold.backend.repository.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Mock
    private UserRepository userRepository;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityConfig = new SecurityConfig(userRepository);
    }

    private OAuth2User githubUser(String id, String login, String name) {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttributes()).thenReturn(Map.of(
                "id", id,
                "login", login,
                "name", name,
                "avatar_url", "https://avatars.githubusercontent.com/u/" + id,
                "html_url", "https://github.com/" + login
        ));
        when(mockOAuth2User.getAttribute("id")).thenReturn(id);
        when(mockOAuth2User.getAttribute("login")).thenReturn(login);
        when(mockOAuth2User.getAttribute("name")).thenReturn(name);
        when(mockOAuth2User.getAttribute("avatar_url")).thenReturn("https://avatars.githubusercontent.com/u/" + id);
        when(mockOAuth2User.getAttribute("html_url")).thenReturn("https://github.com/" + login);
        when(mockOAuth2User.getAuthorities()).thenReturn(Set.of());
        return mockOAuth2User;
    }

    @Test
    void processOAuth2User_existingUser_updatesLastLoginAndDoesNotCreate() {
        UserModel existingUser = new UserModel();
        existingUser.setGithubId("12345");
        existingUser.setUsername("existinguser");
        existingUser.setRole(Role.USER);

        when(userRepository.findByGithubId("12345")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User githubUser = githubUser("12345", "existinguser", "Existing User");

        OAuth2User result = securityConfig.processOAuth2User(githubUser);

        ArgumentCaptor<UserModel> savedUser = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, times(1)).save(savedUser.capture());
        assertEquals(existingUser, savedUser.getValue());
        assertNotNull(savedUser.getValue().getLastLoginAt());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER")));
    }

    @Test
    void processOAuth2User_newUser_createsAndUpdatesLastLogin() {
        when(userRepository.findByGithubId("99999")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User githubUser = githubUser("99999", "newuser", "New User");

        OAuth2User result = securityConfig.processOAuth2User(githubUser);

        ArgumentCaptor<UserModel> savedUser = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository, times(2)).save(savedUser.capture());

        UserModel created = savedUser.getAllValues().get(0);
        assertEquals("99999", created.getGithubId());
        assertEquals("newuser", created.getUsername());
        assertEquals(Role.VIEWER, created.getRole());
        assertNotNull(savedUser.getValue().getLastLoginAt());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("VIEWER")));
    }
}