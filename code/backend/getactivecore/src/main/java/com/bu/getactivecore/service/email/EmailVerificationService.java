package com.bu.getactivecore.service.email;

import com.bu.getactivecore.service.email.api.EmailApi;
import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.exception.ApiException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static com.bu.getactivecore.shared.ErrorCode.EMAIL_INVALID;
import static com.bu.getactivecore.shared.ErrorCode.EMAIL_SEND_FAILED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Service for sending email verifications.
 */
@Slf4j
@Service
public class EmailVerificationService implements EmailApi {

    private final JavaMailSender m_javaEmailSender;

    @Value("${spring.mail.username}")
    private String m_serverEmail;


    /**
     * Constructor for EmailVerificationService.
     *
     * @param javaEmailSender used for sending emails
     */
    public EmailVerificationService(JavaMailSender javaEmailSender) {
        m_javaEmailSender = javaEmailSender;
    }

    @Override
    public void sendVerificationEmail(@NonNull String email, @NonNull String registrationToken) throws ApiException {
        String body = String.format(EmailTemplates.REGISTRATION_TEMPLATE, registrationToken);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(m_serverEmail);
        msg.setTo(email);
        msg.setSubject("GetActive: Registration Verification");
        msg.setText(body);
        try {
            m_javaEmailSender.send(msg);
        } catch (MailParseException e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new ApiException(ApiErrorPayload.builder().status(BAD_REQUEST).errorCode(EMAIL_INVALID)
                    .message("Unable to send verification email to email address: '" + email + "'")
                    .debugMessage(e.getLocalizedMessage())
                    .build()
            );
        } catch (MailSendException e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new ApiException(ApiErrorPayload.builder().status(BAD_REQUEST).errorCode(EMAIL_SEND_FAILED)
                    .message("Unable to send verification email to email address: '" + email + "'")
                    .debugMessage(e.getLocalizedMessage())
                    .build()
            );
        }
    }
}
