package com.katran.gamelogic;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class GameGroup {
    private String id;
    private List<Player> players = new ArrayList<>();
    private boolean gameStarted = false;
    private boolean isFull = false;
    private int size = 0;
    private float amount = 0;
    private long createUserId;



    public GameGroup(String id) {
        this.id = id;
    }

    public boolean isFull() {
        return players.size() >= size;
    }

    public void addPlayer(Player player) {
        if (!isFull()) {
            players.add(player);
        } else {
            throw new IllegalStateException("Game group is full");
        }
    }

    public void startGame() {
        if (!gameStarted) {
            gameStarted = true;
            // Логика запуска игры
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

}

