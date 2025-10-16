package com.NewEmployeeManagement.ServiceImpl;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailService
{
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Password Reset OTP");
            helper.setText("""
                    <h3>Dear User,</h3>
                    <p>Your OTP for password reset is:</p>
                    <h2 style='color:#2e86de;'>%s</h2>
                    <p>This OTP is valid for <b>5 minutes</b>.</p>
                    <p>Do not share this code with anyone.</p>
                    <br/>
                    <p>Regards,<br/>SuperAdmin Support Team</p>
                    """.formatted(otp), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

}
