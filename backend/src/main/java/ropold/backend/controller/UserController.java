package ropold.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import ropold.backend.model.Role;
import ropold.backend.model.UserModel;
import ropold.backend.repository.UserRepository;
import ropold.backend.service.UserService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping(value = "/me", produces = "text/plain")
    public String getMe() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/me/details")
    public Map<String, Object> getUserDetails(@AuthenticationPrincipal OAuth2User authentication) {
        if (authentication == null) {
            return Map.of("message", "User not authenticated");
        }

        try {
            // GitHub ID aus dem "id" Attribut extrahieren
            Object idAttribute = authentication.getAttribute("id");
            String githubId = String.valueOf(idAttribute);

            Optional<UserModel> userOpt = userRepository.findByGithubId(githubId);

            if (userOpt.isEmpty()) {
                UserModel newUser = createUserFromAuthentication(authentication);
                return createUserResponse(newUser);
            }

            UserModel user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            return createUserResponse(user);

        } catch (Exception e) {
            return Map.of("error", "User data temporarily unavailable, please refresh");
        }
    }

    @PostMapping("me/language/{languageIso}")
    public void setPreferredLanguage(@PathVariable String languageIso, @AuthenticationPrincipal OAuth2User authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        Object idAttribute = authentication.getAttribute("id");
        if (idAttribute == null) {
            throw new AccessDeniedException("GitHub ID not found");
        }
        String githubId = String.valueOf(idAttribute);
        userService.setPreferredLanguage(githubId, languageIso);
    }

    private UserModel createUserFromAuthentication(OAuth2User authentication) {
        String githubId = String.valueOf(authentication.getAttribute("id"));
        String username = authentication.getAttribute("login"); // GitHub Username
        String name = authentication.getAttribute("name"); // Display Name
        String avatarUrl = authentication.getAttribute("avatar_url");
        String githubUrl = authentication.getAttribute("html_url");

        UserModel newUser = new UserModel();
        newUser.setGithubId(githubId);
        newUser.setUsername(username);
        newUser.setName(name != null ? name : username);
        newUser.setAvatarUrl(avatarUrl);
        newUser.setGithubUrl(githubUrl);
        newUser.setRole(Role.VIEWER);
        newUser.setPreferredLanguage("de");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(newUser);
    }

    private Map<String, Object> createUserResponse(UserModel user) {
        return Map.of(
                "id", user.getId(),
                "githubId", user.getGithubId() != null ? user.getGithubId() : "",
                "username", user.getUsername(),
                "name", user.getName(),
                "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                "githubUrl", user.getGithubUrl() != null ? user.getGithubUrl() : "",
                "role", user.getRole(),
                "preferredLanguage", user.getPreferredLanguage(),
                "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "",
                "lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : ""
        );
    }
}
