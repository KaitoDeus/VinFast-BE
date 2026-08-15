package com.oem.evwarranty.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(String email, String purpose);
    Optional<OtpVerification> findTopByEmailOrderByCreatedAtDesc(String email);
}
