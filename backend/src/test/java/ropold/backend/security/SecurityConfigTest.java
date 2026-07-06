package ropold.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import ropold.backend.model.UserModel;
import ropold.backend.repository.UserRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

class SecurityConfigTest {
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testOauth2UserService_existingUser() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/login/oauth2/code/github")
                .tokenUri("https://github.com/login/oauth/access_token")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("login")
                .clientName("GitHub")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "mock-token", Instant.now(), Instant.now().plusSeconds(3600)
        );
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttributes()).thenReturn(Map.of(
                "id", "12345",
                "login", "existinguser",
                "name", "Existing User",
                "avatar_url", "https://avatars.githubusercontent.com/u/12345",
                "html_url", "https://github.com/existinguser"
        ));
        when(mockOAuth2User.getName()).thenReturn("existinguser");

        DefaultOAuth2UserService mockUserService = mock(DefaultOAuth2UserService.class);
        when(mockUserService.loadUser(userRequest)).thenReturn(mockOAuth2User);

        when(userRepository.findByGithubId("12345")).thenReturn(Optional.of(new UserModel()));

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService = new SecurityConfig(userRepository) {
            @Override
            public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
                return mockUserService;
            }
        }.oauth2UserService();

        OAuth2User result = oauth2UserService.loadUser(userRequest);

        verify(userRepository, never()).save(any());
        assertEquals(mockOAuth2User, result);
    }
}
