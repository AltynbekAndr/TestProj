package com.katran.controller;

import com.katran.gamelogic.GameGroup;
import com.katran.gamelogic.GameService;
import com.katran.gamelogic.Player;
import com.katran.model.GameWinner;
import com.katran.model.User;
import com.katran.repository.UserRepository;
import com.katran.service.GameWinnerService;
import com.katran.service.PageVisitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PageVisitsService pageVisitsService;

    @GetMapping
    public String showGameForm(Model model,HttpSession session) {
        pageVisitsService.incrementPageVisit("game");
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        } else {
            model.addAttribute("user", new User()); // Для случая, если пользователь не авторизован
        }
        return "game";
    }


    @GetMapping("/currentUser")
    @ResponseBody
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        pageVisitsService.incrementPageVisit("game");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "404");
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        user.setPassword(null);
        user.setPsw(null);
        user.setInpDate(null);
        user.setPromoCode(null);
        return ResponseEntity.ok(user);
    }



    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinGame(@RequestBody Map<String, String> request, HttpSession session) {
        pageVisitsService.incrementPageVisit("game");
        String groupId = request.get("groupId");
        User user = (User) session.getAttribute("user");

        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "5");
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }else if(user!=null && user.getAmount()==0 || user.getAmount()<0){
            Map<String, String> response = new HashMap<>();
            response.put("status", "6");
            response.put("message", "Balance error");
            return ResponseEntity.badRequest().body(response);
        }

        Player player = new Player();
        player.setName(user.getUsername());

        // Получаем информацию о группе, чтобы проверить наличие игрока
        GameGroup group = gameService.getGroupById(groupId); // Метод, который возвращает объект группы
        if (group == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Группа не найдена"));
        }

        // Проверяем, состоит ли пользователь уже в группе
        boolean isUserInGroup = group.getPlayers().stream()
                .anyMatch(existingPlayer -> existingPlayer.getName() != null && existingPlayer.getName().equals(user.getUsername()));

        if (isUserInGroup) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Вы уже присоединены к этой группе"));
        }

        try {
            player.setId(user.getId());
            player.setName(user.getUsername());
            String groupIdResult = gameService.addPlayerToGroup(player, groupId);
            Map<String, String> response = new HashMap<>();
            response.put("groupId", groupIdResult);
            response.put("playerName", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }




    @ResponseBody
    @GetMapping("/group/{groupId}")
    public ResponseEntity<GameGroup> getGroupById(@PathVariable String groupId) {
        pageVisitsService.incrementPageVisit("game");
        GameGroup group = gameService.getGroupById(groupId); // Предполагается, что этот метод уже реализован в вашем сервисе
        if (group == null) {
            return ResponseEntity.notFound().build(); // Возвращаем 404, если группа не найдена
        }

        return ResponseEntity.ok(group); // Возвращаем найденную группу
    }

    @PostMapping("/startGame/{groupId}")
    public ResponseEntity<String> startGame(@PathVariable String groupId) {
        pageVisitsService.incrementPageVisit("game");
        try {
            gameService.startGame(groupId);
            return ResponseEntity.ok("Game started");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/spinWheel/{groupId}")
    public ResponseEntity<String> spinWheel(@PathVariable String groupId) {
        pageVisitsService.incrementPageVisit("game");
        try {
            return gameService.spinWheel(groupId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/groups")
    public ResponseEntity<List<GameGroup>> getGroups(Model model,HttpSession session) {
        pageVisitsService.incrementPageVisit("game");
        List<GameGroup> groups = gameService.getAllGroups();
        User user = (User) session.getAttribute("user");
        if(user!=null){
            model.addAttribute("user",user);
        }
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/createGroup")
    @ResponseBody
    public ResponseEntity<?> createGroup() {
        pageVisitsService.incrementPageVisit("game");
        List<GameGroup> groups = gameService.getAllGroups();

        // Проверяем наличие групп, которые не заполнены
        for (GameGroup group : groups) {
            if (group.getPlayers().size() < 5) { // Максимум 5 игроков
                // Возвращаем информацию о существующей группе, в которую можно присоединиться
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Уже существует группа, в которую можно присоединиться.");
            }
        }
        int numberOfGroupsToCreate = 25; // количество новых групп
        List<GameGroup> newGroups = createNewGroups(numberOfGroupsToCreate);

        // Возвращаем список созданных групп с только ботами и возможностью подключения реального игрока
        return ResponseEntity.ok(newGroups);
    }

    // Метод для создания новых групп с разными игроками и суммами
    private List<GameGroup> createNewGroups(int numberOfGroupsToCreate) {
        List<GameGroup> newGroups = new ArrayList<>();
        Random random = new Random();

        // Создаем список уникальных имен для 200 игроков
        List<String> botNames = new ArrayList<>(List.of(
                "Максим", "Игорь", "Алмаз", "Жако", "Арман",
                "Церкан", "Кокес", "Жаркынай", "Табылды",
                "Женя", "Василий", "Серик", "Бакош",
                "Саламу", "Бахур", "Файза", "Омар",
                "Кирилл", "Наташа", "Владислав", "Ольга",
                "Дмитрий", "Екатерина", "Павел", "Марина",
                "Роман", "Виктория", "Сергей", "Анастасия",
                "Андрей", "Юлия", "Михаил", "Ирина",
                "Евгений", "Татьяна", "Артем", "David", "Spring",
                "Speed", "UZSila", "Танос", "China", "МАГ01", "Гавгав", "ЧеТАМ",
                "BigBoy", "Supermen", "Жафар", "ABU", "Todo", "Ramon",
                "Никита", "Лев", "Арсений", "Глеб", "Марат",
                "Айдар", "Эльдар", "Руслан", "Саша", "Коля",
                "Илья", "Данил", "Петр", "Степан", "Вадим",
                "Алексей", "Станислав", "Филипп", "Юрий", "Тимур",
                "Азамат", "Жан", "Даниил", "Рустам", "Егор",
                "Артур", "Матвей", "Антон", "Денис", "Богдан",
                "Олег", "Иван", "Константин", "Вячеслав", "Альберт",
                "Радмир", "Тарас", "Славик", "Влад", "Евдоким",
                "Аристарх", "Захар", "Клим", "Платон", "Тимофей",
                "Игнат", "Святослав", "Ярослав", "Лука", "Дамир",
                "Эрик", "Виталий", "Ангелина", "Карина", "Алина",
                "Полина", "Вера", "Светлана", "Анна", "Мария",
                "Дарья", "Елена", "Елизавета", "Александра", "Оксана",
                "Любовь", "Тамара", "Людмила", "Кристина", "Валерия",
                "Наталья", "Юлия", "Татьяна", "Ирина", "Нина",
                "Клара", "Регина", "Эмилия", "Лилия", "Виктория",
                "Галина", "София", "Алёна", "Милана", "Евгения",
                "Валентина", "Аврора", "Алла", "Вероника", "Диана",
                "Маргарита", "Яна", "Агата", "Ольга", "Эвелина",
                "Ника", "Лидия", "Серафима", "Ева", "Мира",
                "Аделина", "Ариана", "Злата", "Майя", "Юлиана",
                "Снежана", "Ксения", "Сабина", "Влада", "Анжелика",
                "Амалия", "Инна", "Жанна", "Венера", "Асия",
                "Аяна", "Мадина", "Диляра", "Лейла", "Самира",
                "Наиля", "Роза", "Рената", "Гузель", "Алия"
        ));

        // Перемешиваем список имен для случайного распределения
        Collections.shuffle(botNames);
        Iterator<String> nameIterator = botNames.iterator();

        for (int i = 0; i < numberOfGroupsToCreate; i++) {
            GameGroup gameGroup = gameService.createNewGroup();

            // Устанавливаем количество игроков
            int playersCount = random.nextInt(8) + 2;
            gameGroup.setSize(playersCount);

            // Устанавливаем сумму, чтобы она была кратной 100 и больше 40
            int amount = (random.nextInt(5) + 1) * 100;
            gameGroup.setAmount(amount);

            // Создаем список игроков
            List<Player> players = new ArrayList<>();
            for (int j = 0; j < playersCount; j++) {
                if (nameIterator.hasNext()) {
                    Player bot = new Player();
                    bot.setName(nameIterator.next());
                    players.add(bot);
                } else {
                    throw new IllegalStateException("Недостаточно уникальных имен для создания всех игроков.");
                }
            }

            gameGroup.setPlayers(players); // Устанавливаем игроков в группу
            newGroups.add(gameGroup);
        }

        return newGroups;
    }




    @PostMapping("/newGame")
    public Object newGame(@RequestParam("maxPlayerCount") String maxPlayerCount,
                          @RequestParam("maxAmount") String maxAmount,
                          HttpSession session)              {
        pageVisitsService.incrementPageVisit("game");
        List<GameGroup> groups = gameService.getAllGroups();
        User user = (User) session.getAttribute("user");

        if(user == null){
            Map<String, String> response = new HashMap<>();
            response.put("status", "5");
            response.put("message", "User not found");
            return ResponseEntity.ok(response);
        }else if(user!=null ){
            Optional<User> tmpUser = userRepository.findById(user.getId());
            if(tmpUser!=null){
                if(tmpUser.get().getAmount()<Float.parseFloat(maxAmount)){
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "6");
                    response.put("message", "Balance error");
                    return ResponseEntity.ok(response);
                }else {
                    Player player = new Player();
                    player.setName(user.getUsername());
                    player.setId(user.getId());

                    // Если все группы заполнены, создаем новую
                    GameGroup gameGroup = gameService.createNewGroup();
                    gameGroup.setSize(Integer.parseInt(maxPlayerCount));
                    gameGroup.setAmount(Float.parseFloat(maxAmount));
                    gameGroup.setCreateUserId(player.getId());

                    gameGroup.addPlayer(player);
                    // Возвращаем объект группы или другой нужный ответ
                    return ResponseEntity.ok(groups);
                }
            }
        }
        // Редирект на страницу логина
        return "redirect:/signIn";
    }



    @Autowired
    private GameWinnerService gameWinnerService;

    @ResponseBody
    @PostMapping("/saveGameWinner")
    public Object saveGameWinner(@RequestBody Map<String, String> payload, HttpSession session,Model model) {
        String winnerName = payload.get("winnerName");
        User user = (User) session.getAttribute("user");
        if (user != null) {
            GameWinner gameWinnerV = new GameWinner();
            gameWinnerV.setUsername(user.getUsername());
            gameWinnerV.setAmount(user.getAmount());
            gameWinnerV.setFull_name(user.getFull_name());
            LocalDateTime currentDateTime = LocalDateTime.now();
            gameWinnerV.setInpDate(currentDateTime);
            gameWinnerV.setTotalAmount(user.getTotalAmount());
            GameWinner gameWinner = gameWinnerService.saveGameWinner(gameWinnerV);
            if (gameWinner != null) {

                return "{'status':0}";
            }
        }
        return "redirect:/signIn";
    }






}



