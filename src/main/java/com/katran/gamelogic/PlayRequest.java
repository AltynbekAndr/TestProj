package com.katran.gamelogic;

import java.util.List;

public class PlayRequest {
    private List<String> fruits;
    private int betAmount;

    // Геттеры и сеттеры
    public List<String> getFruits() {
        return fruits;
    }

    public void setFruits(List<String> fruits) {
        this.fruits = fruits;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }
}


