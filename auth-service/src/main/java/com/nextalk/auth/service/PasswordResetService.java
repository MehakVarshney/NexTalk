package com.nextalk.auth.service;

import com.nextalk.auth.dto.ForgotPasswordRequest;
import com.nextalk.auth.dto.ResetPasswordRequest;
import com.nextalk.auth.dto.VerifyOtpRequest;
import com.nextalk.auth.entity.AppUser;
import com.nextalk.auth.entity.OtpToken;
import com.nextalk.auth.exception.ApiException;
import com.nextalk.auth.repository.AppUserRepository;
import com.nextalk.auth.repository.OtpTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    private final AppUserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            AppUserRepository userRepository,
            OtpTokenRepository otpTokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.email().toLowerCase();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User with this email does not exist"));

        // Invalidate previous unused OTPs (optional, but good practice)
        otpTokenRepository.findTopByUserOrderByExpiryDateDesc(user).ifPresent(token -> {
            if (!token.isUsed() && token.getExpiryDate().isAfter(LocalDateTime.now())) {
                token.setUsed(true);
                otpTokenRepository.save(token);
            }
        });

        // Generate 6-digit OTP
        String otp = String.format("%06d", new SecureRandom().nextInt(1000000));
        
        // Save OTP (10 minutes validity)
        OtpToken otpToken = new OtpToken(otp, user, 10);
        otpTokenRepository.save(otpToken);

        // Send Email
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public void verifyOtp(VerifyOtpRequest request) {
        String email = request.email().toLowerCase();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        validateOtp(user, request.otp());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.email().toLowerCase();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        OtpToken otpToken = validateOtp(user, request.otp());

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Mark OTP as used
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
    }

    private OtpToken validateOtp(AppUser user, String otp) {
        OtpToken otpToken = otpTokenRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid OTP"));

        if (otpToken.isUsed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP has already been used");
        }

        if (otpToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        return otpToken;
    }
}
