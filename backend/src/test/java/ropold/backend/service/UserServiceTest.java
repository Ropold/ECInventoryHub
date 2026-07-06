package ropold.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.core.user.OAuth2User;
import ropold.backend.model.Role;
import ropold.backend.model.UserModel;
import ropold.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UUID fixedId;
    private LocalDateTime fixedDate;
    private UserModel testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fixedId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        fixedDate = LocalDateTime.of(2024, 1, 1, 12, 0);
        testUser = new UserModel(
                fixedId,
                "githubId123",
                null,
                "testuser",
                "Test User",
                "https://avatars.githubusercontent.com/u/123456",
                "https://github.com/testuser",
                Role.USER,
                "de",
                fixedDate,
                null
        );
    }

    @Test
    void getUserById_UserExists_ReturnsUser() {
        when(userRepository.findById(fixedId)).thenReturn(Optional.of(testUser));

        UserModel result = userService.getUserById(fixedId);

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository, times(1)).findById(fixedId);
    }

    @Test
    void getUserById_UserDoesNotExist_ThrowsException() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserById(userId));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserByGithubId_UserExists_ReturnsUser() {
        String githubId = "githubId123";
        when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(testUser));

        UserModel result = userService.getUserByGithubId(githubId);

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository, times(1)).findByGithubId(githubId);
    }

    @Test
    void getUserByGithubId_UserDoesNotExist_ThrowsException() {
        String githubId = "nonExistentGitHubId";
        when(userRepository.findByGithubId(githubId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserByGithubId(githubId));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByGithubId(githubId);
    }

    @Test
    void createOrUpdateFromGitHub_UserExists_UpdatesLastLogin() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("id")).thenReturn("githubId123");
        when(userRepository.findByGithubId("githubId123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        UserModel result = userService.createOrUpdateFromGitHub(oAuth2User);

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository, times(1)).findByGithubId("githubId123");
        verify(userRepository, times(1)).save(any(UserModel.class));
    }

    @Test
    void createOrUpdateFromGitHub_UserDoesNotExist_CreatesNewUser() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("id")).thenReturn("newGithubId");
        when(oAuth2User.getAttribute("login")).thenReturn("newuser");
        when(oAuth2User.getAttribute("name")).thenReturn("New User");
        when(userRepository.findByGithubId("newGithubId")).thenReturn(Optional.empty());

        UserModel savedUser = new UserModel(
                UUID.randomUUID(),
                "newGithubId",
                null,
                "newuser",
                "New User",
                null,
                null,
                Role.VIEWER,
                "de",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);

        UserModel result = userService.createOrUpdateFromGitHub(oAuth2User);

        assertNotNull(result);
        assertEquals(savedUser, result);
        verify(userRepository, times(1)).findByGithubId("newGithubId");
        verify(userRepository, times(1)).save(any(UserModel.class));
    }

    @Test
    void setPreferredLanguage_UpdatesLanguage() {
        String githubId = "githubId123";
        String newLanguage = "en";

        doNothing().when(userRepository).updatePreferredLanguage(githubId, newLanguage);

        userService.setPreferredLanguage(githubId, newLanguage);

        verify(userRepository, times(1)).updatePreferredLanguage(githubId, newLanguage);
    }

    @Test
    void setPreferredLanguage_NonExistentUser_DoesNotThrow() {
        String githubId = "nonExistentGitHubId";
        String newLanguage = "en";

        doNothing().when(userRepository).updatePreferredLanguage(githubId, newLanguage);

        assertDoesNotThrow(() -> userService.setPreferredLanguage(githubId, newLanguage));

        verify(userRepository, times(1)).updatePreferredLanguage(githubId, newLanguage);
    }
}