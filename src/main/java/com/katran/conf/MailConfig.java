package com.katran.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Configuration
@Data
public class MailConfig {
    @Value("${mail.username}")
    private String emailSender;

    @Value("${mail.password}")
    private String emailPassword;

    @Value("${mail.host}")
    private String mailHost;


    @Value("${mail.server.base-url}")
    private String serverBaseUrl;

    @Value("${token.expiration.minutes}")
    private int tokenExpirationMinutes;


    private Properties getMailProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.host", mailHost);
        return properties;
    }

    private Session createSession() {
        return Session.getInstance(getMailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailSender, emailPassword);
            }
        });
    }

    public void sendEmail(String toEmail, String subject, String messageText) {
        try {
            Session session = createSession();

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailSender));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);

            System.out.println("Письмо успешно отправлено на адрес: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при отправке письма: " + e.getMessage());
        }
    }
}
