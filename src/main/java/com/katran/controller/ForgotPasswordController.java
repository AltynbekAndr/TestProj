package com.katran.controller;

import com.katran.model.User;
import com.katran.service.PageVisitsService;
import com.katran.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class ForgotPasswordController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    PageVisitsService pageVisitsService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        pageVisitsService.incrementPageVisit("forgot");
        model.addAttribute("user", new User());
        return "forgot-password";
    }

    @PostMapping("/resetpassword")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> payload) {
        pageVisitsService.incrementPageVisit("forgot");
        String email = payload.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Параметр email отсутствует или пуст.");
        }

        try {
            passwordResetService.generateResetTokenAndSendEmail(email);
            return ResponseEntity.ok("Инструкции отправлены.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/reset-password/validate")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        pageVisitsService.incrementPageVisit("forgot");
        if (passwordResetService.validateToken(token)) {
            model.addAttribute("token", token);
            return "reset-password";
        } else {
            model.addAttribute("errorMessage", "Токен не действителен или истёк.");
            return "reset-password";
        }
    }

    @PostMapping("/reset-password/change")
    public ResponseEntity<String> handlePasswordReset(
            @RequestBody Map<String, String> payload
    ) {
        pageVisitsService.incrementPageVisit("forgot");
        String token = payload.get("token");
        String password = payload.get("password");

        if (token == null || password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body("Неверный токен или пароль.");
        }

        try {
            passwordResetService.updatePassword(token, password);
            return ResponseEntity.ok("Пароль успешно обновлён!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
