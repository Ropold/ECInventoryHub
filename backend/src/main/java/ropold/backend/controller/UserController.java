package ropold.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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
    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping(value = "/me", produces = "text/plain")
    public String getMe(@AuthenticationPrincipal OAuth2User authentication) {
        if (authentication == null) {
            return "anonymousUser";
        }
        Object idAttribute = authentication.getAttribute("id");
        return String.valueOf(idAttribute);
    }

    @GetMapping("/me/details")
    public Map<String, Object> getUserDetails(OAuth2AuthenticationToken authenticationToken) {

        if (authenticationToken == null) {
            return Map.of("message", "User not authenticated");
        }

        OAuth2User authentication = authenticationToken.getPrincipal();

        try {
            // GitHub ID aus dem "id" Attribut extrahieren, damit unser eigenes UserModel aktuell bleibt
            Object idAttribute = authentication.getAttribute("id");
            String githubId = String.valueOf(idAttribute);

            Optional<UserModel> userOpt = userRepository.findByGithubId(githubId);
            UserModel user = userOpt.orElseGet(() -> createUserFromAuthentication(authentication));
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    authenticationToken.getAuthorizedClientRegistrationId(),
                    authenticationToken.getName());

            if (authorizedClient == null) {
                return Map.of("error", "User data temporarily unavailable, please refresh");
            }

            // Vollständiges GitHub-Profil (login, bio, followers, public_repos, ...) live abrufen
            String accessToken = authorizedClient.getAccessToken().getTokenValue();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github+json");

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody();

        } catch (Exception e) {
            return Map.of("error", "User data temporarily unavailable, please refresh");
        }
    }

    @GetMapping(value = "/me/language", produces = "text/plain")
    public String getPreferredLanguage(@AuthenticationPrincipal OAuth2User authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        Object idAttribute = authentication.getAttribute("id");
        if (idAttribute == null) {
            throw new AccessDeniedException("GitHub ID not found");
        }
        String githubId = String.valueOf(idAttribute);
        return userService.getUserByGithubId(githubId).getPreferredLanguage();
    }

    @GetMapping(value = "/me/role", produces = "text/plain")
    public String getMyRole(@AuthenticationPrincipal OAuth2User authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        Object idAttribute = authentication.getAttribute("id");
        if (idAttribute == null) {
            throw new AccessDeniedException("GitHub ID not found");
        }
        String githubId = String.valueOf(idAttribute);
        return userService.getUserByGithubId(githubId).getRole().name();
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
        Object idAttribute = authentication.getAttribute("id");
        String githubId = String.valueOf(idAttribute);
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
}
