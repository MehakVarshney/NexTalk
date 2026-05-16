package com.nextalk.auth.repository;

import com.nextalk.auth.entity.AppUser;
import com.nextalk.auth.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {
    Optional<OtpToken> findByOtpAndUser(String otp, AppUser user);
    Optional<OtpToken> findTopByUserOrderByExpiryDateDesc(AppUser user);
}
