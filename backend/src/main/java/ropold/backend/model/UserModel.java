package ropold.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "github_id", nullable = false, unique = true)
    private String githubId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.VIEWER;

    @Column(name = "preferred_language", length = 2)
    private String preferredLanguage = "de";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}