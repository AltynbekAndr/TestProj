package com.katran.controller;

import com.katran.model.Accounts;
import com.katran.model.SendMoney;
import com.katran.model.Stock;
import com.katran.model.User;
import com.katran.repository.AccountsRepository;
import com.katran.repository.SendMoneyRepository;
import com.katran.repository.UserRepository;
import com.katran.service.PageVisitsService;
import com.katran.service.StockService;
import com.katran.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Controller
public class MainIndexController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    PageVisitsService pageVisitsService;

    @Autowired
    private OnlineUsersCache onlineUsersCache;

    @Autowired
    private SendMoneyRepository sendMoneyRepository;

    // Метод для отображения страницы главного индекса
    @GetMapping("/mainIndex")
    public String showMainIndexForm(HttpSession session, Model model) {
        pageVisitsService.incrementPageVisit("main");
        User user = (User) session.getAttribute("user");
        Stock stock = stockService.getLastStockRecord();

        if (user != null) {
            Optional<User> tmpUser = userRepository.findById(user.getId());
            session.setAttribute("user", tmpUser.get());
            model.addAttribute("user", tmpUser.get());
            long userId = user.getId();
            getAccountsByUserId(model, userId);
        }

        model.addAttribute("onlineUsers", onlineUsersCache.getOnlineUsers());
        model.addAttribute("stock", stock);
        return "mainIndex";
    }

    // Метод для обработки запроса на вывод средств
    @PostMapping("/sendMoney")
    public String handleWithdrawal(
            @RequestParam(required = false) float amount,
            @RequestParam(required = false) String swiftCode,
            @RequestParam(required = false) String bankAccount,
            @RequestParam(required = false) String fullname,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String cryptoAddress,
            @RequestParam(required = false) String cryptoType,
            @RequestParam(required = false) String additionalInfo,
            HttpSession session,
            Model model) {

        System.out.println(amount);
        System.out.println(swiftCode);
        System.out.println(bankAccount);
        System.out.println(fullname);
        System.out.println(cardNumber);



        pageVisitsService.incrementPageVisit("main");
        User user = (User) session.getAttribute("user");

        // Проверка, что пользователь авторизован
        if (user == null) {
            model.addAttribute("error", "Пожалуйста, авторизуйтесь для выполнения операции.");
            return "mainIndex";
        }

        session.setAttribute("user", user);
        model.addAttribute("user", user);

        // Проверка наличия данных для вывода средств
        if (swiftCode == null && cardNumber == null && cryptoAddress == null) {
            model.addAttribute("error", "Пожалуйста, выберите способ вывода средств.");
            return "mainIndex";
        }

        // Здесь можно добавить логику для обработки конкретных типов перевода
        SendMoney sendMoney = new SendMoney();
        if(swiftCode!=null){
            sendMoney.setSwiftCode(swiftCode);
        }
        if(bankAccount!=null){
            sendMoney.setBankAccount(bankAccount);
        }
        if(fullname!=null){
            sendMoney.setFullname(fullname);
        }
        if(cardNumber!=null){
            sendMoney.setCardNumber(cardNumber);
        }
        if(cryptoAddress!=null){
            sendMoney.setCryptoAddress(cryptoAddress);
        }
        if(cryptoType!=null){
            sendMoney.setCryptoType(cryptoType);
        }
        if(additionalInfo!=null){
            sendMoney.setAdditionalInfo(additionalInfo);
        }
        sendMoneyRepository.save(sendMoney);
        model.addAttribute("message", "Запрос на вывод средств успешно принят.");
        return "mainIndex";
    }

    // Метод для получения аккаунтов пользователя
    public void getAccountsByUserId(Model model, long userId) {
        List<Accounts> userAccounts = accountsRepository.findByUserId(userId);
        if (userAccounts == null || userAccounts.isEmpty()) {
            model.addAttribute("error", "У вас нет доступных аккаунтов для вывода средств.");
        } else {
            model.addAttribute("accounts", userAccounts);
        }
    }

    // Метод для получения курса валют с внешнего API
    public float getRubToUsdRate(float amount, HttpSession session) throws Exception {
        String apiKey = "b64ac2570d699f826ef303511bb195ff";
        String endpoint = "https://open.er-api.com/v6/latest/RUB";

        try {
            URL url = new URL(endpoint + "?apikey=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONObject rates = jsonObject.getJSONObject("rates");
                float usdRate = rates.getFloat("USD");
                session.setAttribute("rate", usdRate);
                return usdRate * amount;
            } else {
                throw new Exception("Ошибка при получении данных с API: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Не удалось получить курс валют. Попробуйте позже.");
        }
    }
}
