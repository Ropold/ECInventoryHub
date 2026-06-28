package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ropold.backend.model.Role;
import ropold.backend.model.UserModel;
import ropold.backend.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserModel getUserByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserModel getUserById(UUID fixedId) {
        return userRepository.findById(fixedId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void setPreferredLanguage(String githubId, String languageIso) {
        userRepository.updatePreferredLanguage(githubId, languageIso);
    }

    public UserModel createOrUpdateFromGitHub(OAuth2User oAuth2User) {
        String githubId = oAuth2User.getAttribute("id");

        return userRepository.findByGithubId(githubId)
                .map(existingUser -> {
                    existingUser.setLastLoginAt(java.time.LocalDateTime.now());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    UserModel newUser = new UserModel(
                            UUID.randomUUID(),
                            githubId,
                            null,
                            oAuth2User.getAttribute("login"),
                            oAuth2User.getAttribute("name"),
                            oAuth2User.getAttribute("avatar_url"),
                            oAuth2User.getAttribute("html_url"),
                            Role.USER,
                            "de",
                            java.time.LocalDateTime.now(),
                            java.time.LocalDateTime.now()
                    );
                    return userRepository.save(newUser);
                });
    }
}
