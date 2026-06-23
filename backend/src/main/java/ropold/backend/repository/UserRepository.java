package ropold.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ropold.backend.model.UserModel;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByGithubId(String githubId);

    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.preferredLanguage = :language WHERE u.githubId = :githubId")
    void updatePreferredLanguage(@Param("githubId") String githubId, @Param("language") String language);
}
