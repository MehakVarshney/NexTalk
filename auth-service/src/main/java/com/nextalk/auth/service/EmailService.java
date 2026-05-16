package com.nextalk.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@nextalk.com");
            message.setTo(to);
            message.setSubject("NexTalk - Password Reset OTP");
            message.setText("Your OTP for password reset is: " + otp + "\n\nThis OTP is valid for 10 minutes.");

            mailSender.send(message);
            logger.info("OTP email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}. Check SMTP configuration in application.yml.", to, e);
        }
    }
}
