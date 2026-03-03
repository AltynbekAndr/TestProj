package com.katran.gamelogic;

import com.katran.conf.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WebSocketSessionManager sessionManager;

    private List<GameGroup> gameGroups = new ArrayList<>();

    public List<String> getGamerNames(String groupId) {
        for (GameGroup group : gameGroups) {
            if (group.getId().equals(groupId)) {
                return group.getPlayers()
                        .stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }


    public void addGroup(GameGroup gameGroup) {
        gameGroups.add(gameGroup);
    }

    public GameGroup getGroupById(String groupId) {
        for (GameGroup group : gameGroups) {
            if (group.getId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    public GameGroup createNewGroup() {
        GameGroup newGroup = new GameGroup(generateGroupId());
        newGroup.setId(UUID.randomUUID().toString());
        gameGroups.add(newGroup);
        return newGroup;
    }

    public void startGame(String groupId) {
        GameGroup group = gameGroups.stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        group.startGame();
    }

    public ResponseEntity<String> spinWheel(String groupId) {
        GameGroup group = gameGroups.stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElse(null);

        if (group == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group not found");
        }

        if (!group.isGameStarted()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game has not started yet");
        }

        String result = startWheel(group);
        return ResponseEntity.ok(result);
    }

    private String startWheel(GameGroup group) {
        String winner = group.getPlayers().get(new Random().nextInt(group.getPlayers().size())).getName();
        return "The winner is: " + winner;
    }

    public List<GameGroup> getAllGroups() {
        return gameGroups;
    }

    public String addPlayerToGroup(Player player, String groupId) {
        for (GameGroup group : gameGroups) {
            if (group.getPlayers().contains(player)) {
                group.getPlayers().remove(player);
            }
        }

        GameGroup group;

        if (groupId != null && !groupId.isEmpty()) {
            group = findGroupById(groupId);
            if (group != null && group.getPlayers().size() < 10) {
                group.getPlayers().add(player);
                if (group.getPlayers().size() == 1) {
                    group.setCreateUserId(player.getId());
                }
                return group.getId();
            }
        }

        group = gameGroups.stream()
                .filter(g -> g.getPlayers().size() < 10)
                .findFirst()
                .orElse(null);

        if (group == null) {
            group = new GameGroup(generateGroupId());
            gameGroups.add(group);
        }

        group.getPlayers().add(player);
        if (group.getPlayers().size() == 1) {
            group.setCreateUserId(player.getId());
        }
        return group.getId();
    }

    public GameGroup findGroupById(String groupId) {
        return gameGroups.stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst()
                .orElse(null);
    }

    public void removeGroupById(String groupId) {
        GameGroup groupToRemove = null;
        for (GameGroup group : gameGroups) {
            if (group.getId().equals(groupId)) {
                groupToRemove = group;
                break;
            }
        }

        if (groupToRemove != null) {
            gameGroups.remove(groupToRemove);
            System.out.println("Группа с ID " + groupId + " успешно удалена.");
            sessionManager.sendMessageToAll("gameOver:" + groupId);
        } else {
            System.err.println("Группа с ID " + groupId + " не найдена.");
        }
    }

    // Метод для перевода средств между игроками (с проигравшего на победителя)
    public void transferAmount(Long loserId, Long winnerId, Float amount) {
        // Проверяем, достаточно ли средств у проигравшего игрока
        if (!hasSufficientFunds(loserId, amount)) {
            throw new IllegalArgumentException("Недостаточно средств у проигравшего игрока");
        }

        // Переводим средства с проигравшего на победителя
        jdbcTemplate.update("UPDATE users SET amount = amount + ? WHERE id = ?", amount, winnerId); // добавляем победителю
        jdbcTemplate.update("UPDATE users SET amount = amount - ? WHERE id = ?", amount, loserId);  // уменьшаем у проигравшего

        // Логируем успешный перевод
        System.out.println("Перевод ставки " + amount + " от игрока с ID " + loserId + " на игрока с ID " + winnerId);
    }


    // Метод для проверки наличия достаточного баланса у пользователя
    private boolean hasSufficientFunds(Long userId, Float amount) {
        String sql = "SELECT amount FROM users WHERE id = ?";
        Float currentAmount = jdbcTemplate.queryForObject(sql, Float.class, userId);
        return currentAmount != null && currentAmount >= amount;
    }

    private String generateGroupId() {
        return UUID.randomUUID().toString();
    }
}
