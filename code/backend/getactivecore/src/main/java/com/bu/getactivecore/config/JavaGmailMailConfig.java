package com.bu.getactivecore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuration class for setting up JavaMailSender with Gmail SMTP.
 * <p>
 * Retrieves the mail password from application properties and configures
 * the sender with SMTP host, port, authentication, and TLS settings.
 */
@Configuration
public class JavaGmailMailConfig {

    @Value("${spring.mail.password}")
    private String m_password;

    /**
     * Configures and returns a JavaMailSender bean for sending emails via Gmail SMTP.
     *
     * @return a fully configured {@link JavaMailSender} instance
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl emailSender = new JavaMailSenderImpl();
        emailSender.setHost("smtp.gmail.com");
        emailSender.setPort(587);
        emailSender.setUsername("cs673getactive@gmail.com");
        emailSender.setPassword(m_password);

        Properties props = emailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return emailSender;
    }

}
