/*
package ropold.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import ropold.backend.repository.UserRepository;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("!test")
public class SecurityConfig {

    @Value("${app.url}")
    private String appUrl;

    private final UserRepository userRepository;
    private static final String COUNTRY = "/api/countries/**";
    private static final String USER = "/api/users/**";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET, COUNTRY, USER).permitAll()
                        .requestMatchers(HttpMethod.POST, COUNTRY, USER).authenticated()
                        .requestMatchers(HttpMethod.PUT, COUNTRY, USER).authenticated()
                        .requestMatchers(HttpMethod.DELETE, COUNTRY, USER).authenticated()
                        .anyRequest().permitAll()
                )
                .logout(l -> l.logoutUrl("/api/users/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200)))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(o -> o.defaultSuccessUrl(appUrl)
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService())));

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService userService = new DefaultOAuth2UserService();

        return (userRequest) -> {
            OAuth2User githubUser = userService.loadUser(userRequest);

            // GitHub Attribute extrahieren
            Object githubIdObj = githubUser.getAttribute("id");
            if (githubIdObj == null) {
                throw new IllegalStateException("GitHub ID not found in authentication");
            }
            String githubId = String.valueOf(githubIdObj);

            String username = githubUser.getAttribute("login"); // GitHub Username
            String name = githubUser.getAttribute("name"); // Display Name
            String avatarUrl = githubUser.getAttribute("avatar_url");
            String githubUrl = githubUser.getAttribute("html_url");

            // Lade oder erstelle User ohne gleichzeitiges Update
            UserModel user = userRepository.findByGithubId(githubId)
                    .orElseGet(() -> {
                        UserModel newUser = new UserModel();
                        newUser.setGithubId(githubId);
                        newUser.setUsername(username);
                        newUser.setName(name != null ? name : username); // Fallback auf username
                        newUser.setRole("USER");
                        newUser.setPreferredLanguage("de");
                        newUser.setCreatedAt(LocalDateTime.now());
                        newUser.setLastLoginAt(LocalDateTime.now());
                        newUser.setAvatarUrl(avatarUrl);
                        newUser.setGithubUrl(githubUrl);

                        return userRepository.save(newUser);
                    });

            return githubUser;
        };
    }
*/
