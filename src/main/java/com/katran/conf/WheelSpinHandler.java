package com.katran.conf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.katran.gamelogic.GameGroup;
import com.katran.gamelogic.GameService;
import com.katran.gamelogic.Player;
import com.katran.model.GameWinner;
import com.katran.repository.GameWinnerRepository;
import com.katran.service.GameWinnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;
import java.util.Random;

@Component
public class WheelSpinHandler extends TextWebSocketHandler {

    @Autowired
    private WebSocketSessionManager sessionManager;

    @Autowired
    private GameWinnerRepository gameWinnerRepository;

    @Autowired
    private GameService gameService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Добавляем сессию в менеджер
        sessionManager.addSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Удаляем сессию из менеджера
        sessionManager.removeSession(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println(payload);

        if (payload.startsWith("startWheel:")) {
            int groupIdIndex = payload.indexOf(":");
            String groupIdSubstring = payload.substring(groupIdIndex + 1);

            // Получаем участников группы
            List<String> participants = gameService.getGamerNames(groupIdSubstring);

            // Формируем сообщение
            String participantsJson = new ObjectMapper().writeValueAsString(participants); // Преобразуем в JSON
            sessionManager.sendMessageToAll("wheelStart:" + groupIdSubstring + ":" + participantsJson);
        }

        if (payload.startsWith("getWinner:")) {
            try {
                String groupId = extractGroupId(payload);
                List<String> participants = gameService.getGamerNames(groupId);
                if (participants == null || participants.isEmpty()) {
                    throw new IllegalStateException("Группа " + groupId + " не найдена или не содержит участников");
                }
                int winnerIndex = new Random().nextInt(participants.size());
                String winnerName = participants.get(winnerIndex); // Используем имя победителя
                Player winner = findPlayerByName(groupId, winnerName);
                if (winner == null) {
                    throw new IllegalStateException("Победитель не найден в группе");
                }
                List<Player> losers = new ArrayList<>();
                for (int i = 0; i < participants.size(); i++) {
                    if (i != winnerIndex) {
                        Player loser = findPlayerByName(groupId, participants.get(i));
                        if (loser == null) {
                            throw new IllegalStateException("Проигравший игрок не найден в группе");
                        }
                        losers.add(loser);
                    }
                }

                GameGroup group = gameService.findGroupById(groupId);
                if (group == null) {
                    throw new IllegalStateException("Группа с ID " + groupId + " не найдена");
                }

                Float betAmount = group.getAmount();
                if (betAmount == null || betAmount <= 0) {
                    throw new IllegalStateException("Ставка не установлена или равна 0");
                }
                float totalWinAmount = 0;
                for (Player loser : losers) {
                    gameService.transferAmount(loser.getId(), winner.getId(), betAmount);
                    totalWinAmount += betAmount;
                }
                GameWinner gameWinner = new GameWinner();
                gameWinner.setUsername(winnerName);
                gameWinner.setFull_name(winner.getName());
                gameWinner.setAmount(betAmount);
                gameWinner.setInpDate(LocalDateTime.now());
                gameWinner.setTotalAmount(totalWinAmount);
                gameWinnerRepository.save(gameWinner);
                String participantsJson = new ObjectMapper().writeValueAsString(participants);
                sessionManager.sendMessageToAll("winner:" + groupId +  ":" + winnerIndex + ":" + totalWinAmount+ ":" + participantsJson);
                System.out.println("winner:" + groupId + ":" + winnerIndex + ":" + totalWinAmount+ ":" + participantsJson);

                gameService.removeGroupById(groupId);
            } catch (Exception e) {
                System.err.println("Ошибка при обработке команды getWinner: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Обработка добавления игрока в игру
        if (payload.startsWith("addPlayer:")) {
            String[] parts = payload.split(":");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Некорректный формат команды addPlayer");
            }

            String groupId = parts[1];

            // Получаем обновленный список игроков
            List<String> participants = gameService.getGamerNames(groupId);

            // Отправляем обновленный список всем клиентам
            String participantsJson = new ObjectMapper().writeValueAsString(participants);
            sessionManager.sendMessageToAll("playerUpdate:" + groupId + ":" + participantsJson);
        }
    }


    private String extractGroupId(String payload) {
        int groupIdIndex = payload.indexOf(":");
        return payload.substring(groupIdIndex + 1);
    }

    private Player findPlayerByName(String groupId, String playerName) {
        GameGroup group = gameService.getGroupById(groupId);
        if (group != null) {
            return group.getPlayers().stream()
                    .filter(player -> player.getName().equals(playerName))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}





