package com.katran.controller;

import com.katran.model.User;
import com.katran.repository.UserRepository;
import com.katran.service.PageVisitsService;
import com.katran.service.PromoCodeService;
import com.katran.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;
    @Autowired
    private PromoCodeService promoCodeService; // Сервис для генерации промокодов

    @Autowired
    PageVisitsService pageVisitsService;

    @GetMapping("/registration")
    public String showRegistrationPage(@RequestParam(value = "promoCode", required = false) String promoCode,
                                       @RequestParam(value = "error", required = false) String error,
                                       Model model) {
        pageVisitsService.incrementPageVisit("registration");
        model.addAttribute("promoCode", promoCode); // Передаём параметр promoCode в модель
        model.addAttribute("error", error); // Передаём параметр error в модель
        return "registration"; // Имя HTML-шаблона
    }

    @PostMapping("/registration")
    public String registerUser(@ModelAttribute("user") User user, HttpSession session, HttpServletRequest request) {
        pageVisitsService.incrementPageVisit("registration");
        // Проверка, если пользователь с таким username уже существует
        if (userService.findByUsername(user.getUsername()) != null) {
            return "redirect:/registration?error=username"; // Ошибка: имя пользователя занято
        }

        // Проверка на обязательные поля
        if (user.getUsername() == null || user.getUsername().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getFull_name() == null || user.getFull_name().isEmpty() ||
                user.getCountry() == null || user.getCountry().isEmpty()) {
            return "redirect:/registration?error=fields"; // Ошибка: незаполненные поля
        }

        // Проверка длины пароля
        if (user.getPassword().length() < 6) {
            return "redirect:/registration?error=password"; // Ошибка: короткий пароль
        }

        // Проверка формата email
        if (!user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return "redirect:/registration?error=emailFormat"; // Ошибка: неверный формат email
        }

        // Если был передан промокод, устанавливаем его в поле пользователя
        if (user.getPromoCode() != null && !user.getPromoCode().isEmpty()) {
            // Ищем пользователя по промокоду (если нужно)
            User referrer = userService.findByPromoCode(user.getPromoCode()); // Это может быть реализация поиска пользователя по промокоду
            if (referrer != null) {
                // Устанавливаем промокод пригласившего пользователя
                user.setReferralCode(referrer.getPromoCode());
                // Получаем количество пользователей, привлеченных по промокоду реферера
                long countUsers = userService.countByReferredByPromoCode(referrer.getPromoCode());
                // Определяем новую сумму для реферера в зависимости от количества привлеченных пользователей
                float newAmount = referrer.getAmount(); // Начальная сумма
                if (countUsers <= 5) {
                    newAmount += 25; // За первых 5 пользователей добавляется 50
                } else {
                    newAmount += 50;
                }
                // Обновляем поле amount у реферера
                referrer.setAmount(newAmount);
                // Обновляем данные реферера в базе данных
                userService.updateTotalAmount(referrer.getId(), newAmount);
            }
        }

        // Генерация собственного промокода для нового пользователя
        String generatedPromoCode = promoCodeService.generatePromoCode();
        user.setPromoCode(generatedPromoCode); // Устанавливаем собственный промокод для нового пользователя
        user.setPsw(user.getPassword());


        ZoneId bishkekZone = ZoneId.of("Asia/Bishkek");
        ZonedDateTime bishkekTime = ZonedDateTime.now(bishkekZone);

        // Установка начального баланса
        user.setInpDate(bishkekTime.truncatedTo(ChronoUnit.SECONDS));
        user.setAmount(0f);
        String clientIp = getClientIp(request);
        user.setIp(clientIp);
        long randomAccountNumber = generateRandomAccountNumber();
        user.setAcc_number(String.valueOf(randomAccountNumber));


        // Сохранение нового пользователя
        userService.save(user);

        // Добавление пользователя в сессию
        session.setAttribute("user", user);

        // Перенаправление на страницу входа
        return "redirect:/signIn";

    }
    private long generateRandomAccountNumber() {
        Random random = new Random();
        return (long) (1000000000000000L + (random.nextDouble() * 9000000000000000L));
    }

    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // В случае, если клиент за несколькими прокси, берем первый IP
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }





}












