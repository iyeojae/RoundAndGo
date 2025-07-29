package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByToken(String token);
    void deleteByEmail(String email); // 중복 방지용
}
