package com.katran.controller;

import com.katran.model.Accounts;
import com.katran.model.User;
import com.katran.repository.AccountsRepository;
import com.katran.repository.UserRepository;
import com.katran.service.PageVisitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("profile")
public class ProfileController {
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    PageVisitsService pageVisitsService;


    @GetMapping
    public String showProfileForm(HttpSession session, Model model) {
        pageVisitsService.incrementPageVisit("profile");
        // Получаем объект user из сессии
        User user = (User) session.getAttribute("user");

        // Проверяем, есть ли пользователь в сессии
        if (user != null) {
            model.addAttribute("user", user);
            long userId = user.getId();  // Достаем userId из объекта user
            getAccountsByUserId(model, userId); // Ваш существующий метод

            // Получаем промокод пользователя
            String promoCode = user.getPromoCode();

            // Вызываем метод для подсчёта рефералов
            long referralCount = userRepository.countByReferredByPromoCode(promoCode);

            // Добавляем количество рефералов в модель
            model.addAttribute("referralCount", referralCount);

            // Получаем список привлечённых игроков
            List<User> referrals = userRepository.getReferralsByPromoCode(promoCode);
            model.addAttribute("referrals", referrals);
        } else {
            return "redirect:/signIn";
        }

        return "profile";  // Отображаем шаблон profile
    }
    @PostMapping("/save-profile")
    public String saveProfile(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("country") String country,
            HttpSession session,
            Model model) {
        pageVisitsService.incrementPageVisit("profile");
        // Получаем объект user из сессии
        User user = (User) session.getAttribute("user");

        // Проверяем, есть ли пользователь в сессии
        if (user != null) {
            // Устанавливаем обновленные данные пользователя
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setPsw(password);
            user.setCountry(country);

            // Сохраняем изменения в базе данных
            userRepository.save(user);

            // Обновляем модель с обновленными данными пользователя
            getAccountsByUserId(model, user.getId());
        } else {
            // Если пользователя нет в сессии, редиректим на страницу логина
            return "redirect:/signIn";
        }

        // Перенаправляем обратно на страницу профиля после сохранения
        return "redirect:/profile";
    }


    @PostMapping("/save-card-account")
    public String saveAccount(@RequestParam("cardNumber") String cardNumber, HttpSession session, Model model) {
        pageVisitsService.incrementPageVisit("profile");
        // Получаем объект user из сессии
        User user = (User) session.getAttribute("user");

        // Проверяем, есть ли пользователь в сессии
        if (user != null) {
            long userId = user.getId();  // Достаем userId из объекта user

            // Создаем новый объект счета и заполняем его данными
            Accounts newAccount = new Accounts();
            newAccount.setUserId(userId);  // Устанавливаем ID пользователя
            newAccount.setAccountNumber(cardNumber);  // Устанавливаем номер карты
            newAccount.setAccountType("CARD");  // Пример типа счета
            newAccount.setAmount(0);  // Устанавливаем начальную сумму 0
            accountsRepository.save(newAccount);

            // Обновляем данные счета в модели
            getAccountsByUserId(model, userId);
        } else {
            // Если пользователя нет в сессии, редиректим на страницу логина
            return "redirect:/signIn";
        }

        // Перенаправляем обратно на страницу профиля, чтобы избежать дублирования URL
        return "redirect:/profile";
    }



    @PostMapping("/save-swift-account")
    public String saveSwiftAccount(
            @RequestParam("swift-code") String swiftCode,
            @RequestParam("swift-name") String swiftName,
            @RequestParam("swift-account") String swiftAccount,
            @RequestParam("additional-info") String additionalInfo,
            HttpSession session,
            Model model) {
        pageVisitsService.incrementPageVisit("profile");
        // Получаем объект user из сессии
        User user = (User) session.getAttribute("user");

        // Проверяем, есть ли пользователь в сессии
        if (user != null) {
            long userId = user.getId();  // Достаем userId из объекта user

            // Создаем новый объект SWIFT счета
            Accounts newSwiftAccount = new Accounts();
            newSwiftAccount.setUserId(userId);  // Устанавливаем ID пользователя
            newSwiftAccount.setAccountNumber(swiftAccount);  // Устанавливаем номер банковского счета
            newSwiftAccount.setInfo(additionalInfo);  // Устанавливаем номер банковского счета
            newSwiftAccount.setCustomerName(swiftName);  // Устанавливаем номер банковского счета
            newSwiftAccount.setAccountType("SWIFT: " + swiftCode);  // Формируем тип счета с SWIFT кодом и ФИО владельца
            newSwiftAccount.setAmount(0);  // Устанавливаем начальную сумму на счету (0)

            // Сохраняем новый SWIFT счет в базе данных
            accountsRepository.save(newSwiftAccount);

            // Обновляем данные счетов в модели
            getAccountsByUserId(model, userId);
        } else {
            // Если пользователя нет в сессии, редиректим на страницу логина
            return "redirect:/signIn";
        }

        // Перенаправляем обратно на страницу профиля, чтобы избежать дублирования URL
        return "redirect:/profile";
    }


    @PostMapping("/save-crypto-account")
    public String saveCryptoAccount(
            @RequestParam("crypto-type") String cryptoType,
            @RequestParam("crypto-wallet") String cryptoWallet,
            HttpSession session,
            Model model) {
        pageVisitsService.incrementPageVisit("profile");
        // Получаем объект user из сессии
        User user = (User) session.getAttribute("user");

        // Проверяем, есть ли пользователь в сессии
        if (user != null) {
            long userId = user.getId();  // Достаем userId из объекта user

            // Создаем новый объект криптосчета
            Accounts newCryptoAccount = new Accounts();
            newCryptoAccount.setUserId(userId);  // Устанавливаем ID пользователя
            newCryptoAccount.setAccountNumber(cryptoWallet);  // Устанавливаем адрес кошелька
            newCryptoAccount.setAccountType("Crypto: " + cryptoType);  // Формируем тип счета с указанием криптовалюты
            newCryptoAccount.setAmount(0);  // Устанавливаем начальную сумму на счету (0)

            // Сохраняем новый криптосчет в базе данных
            accountsRepository.save(newCryptoAccount);

            // Обновляем данные счетов в модели
            getAccountsByUserId(model, userId);
        } else {
            // Если пользователя нет в сессии, редиректим на страницу логина
            return "redirect:/signIn";
        }

        // Перенаправляем обратно на страницу профиля, чтобы избежать дублирования URL
        return "redirect:/profile";
    }





    public void getAccountsByUserId(Model model,long userId){
        List<Accounts> userAccounts = accountsRepository.findByUserId(userId);
        model.addAttribute("accounts", userAccounts);
    }




}
