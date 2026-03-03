package com.katran.controller;

import com.katran.model.User;
import com.katran.service.PageVisitsService;
import com.katran.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Controller
public class SignInController {

    @Autowired
    private UserService userService;

    @Autowired
    PageVisitsService pageVisitsService;

    @Autowired
    private OnlineUsersCache onlineUsersCache;

    @GetMapping("/signIn")
    public String showSignInForm() {
        pageVisitsService.incrementPageVisit("signin");
        return "signIn";
    }



    @PostMapping("/signIn")
    public String signIn(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         HttpSession session, Model model) {
        pageVisitsService.incrementPageVisit("signin");
        User user = userService.authenticate(username, password);

        if (user != null) {
            session.setAttribute("user", user);
            model.addAttribute("user", user);
            model.addAttribute("onlineUsers", onlineUsersCache.getOnlineUsers());
            return "mainIndex"; // Перенаправление на главную страницу
        } else {
            // Ошибка аутентификации
            model.addAttribute("error", "invalidCredentials");
            return "signIn"; // Вернуться на страницу входа
        }
    }





    public float getRubToUsdRate(float amount,HttpSession session) throws Exception {
        String apiKey = "b64ac2570d699f826ef303511bb195ff";
        String endpoint = "https://open.er-api.com/v6/latest/RUB";

        try {
            // Формируем URL с API ключом
            URL url = new URL(endpoint + "?apikey=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Проверяем успешность ответа
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Парсим JSON-ответ
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONObject rates = jsonObject.getJSONObject("rates");
                float usdRate = rates.getFloat("USD"); // Получаем курс USD

                System.out.println("Курс USD: " + usdRate);
                session.setAttribute("rate",usdRate);
                return usdRate * amount; // Умножаем на сумму
            } else {
                System.out.println("Ошибка: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return amount; // Если произошла ошибка, возвращаем исходную сумму
    }





}
