package com.katran.service;

import com.katran.conf.MailConfig;
import com.katran.model.User;
import com.katran.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailConfig mailConfig;


    public boolean validateToken(String token) {
        Optional<User> userOptional = userRepository.findByResetToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getTokenExpiration().isAfter(LocalDateTime.now())) {
                return true;
            }
        }
        return false;
    }

    public void updatePassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetToken(token);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Пользователь с указанным токеном не найден.");
        }

        User user = userOptional.get();
        if (validateToken(token)) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setTokenExpiration(null);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Токен истёк или недействителен.");
        }
    }


    @Transactional
    public String generateResetTokenAndSendEmail(String username) {
        User user = userRepository.findByUsername(username);
        if (Objects.nonNull(user)) {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setTokenExpiration(LocalDateTime.now().plusMinutes(5));
            userRepository.save(user);

            String resetLink = mailConfig.getServerBaseUrl() + "/reset-password/validate?token=" + token;

            mailConfig.sendEmail(username, "Сброс пароля", resetLink);
            return resetLink;
        } else {
            throw new IllegalArgumentException("Пользователь с указанным username не найден.");
        }
    }
}
