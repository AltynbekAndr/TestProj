package com.katran.gamelogic;

import lombok.Data;

@Data
public class Player {
    private long id;
    private String name;
    private Float bet; // Ставка игрока
    private Float balance; // Баланс игрока

    public Player() {
        // Инициализируем баланс в 0, если не указан
        this.balance = 0f;
    }

    public Player(long id, String name, Float bet, Float balance) {
        this.id = id;
        this.name = name;
        this.bet = bet;
        this.balance = balance;
    }

    // Геттеры и сеттеры
    public Float getBet() {
        return bet;
    }

    public void setBet(Float bet) {
        this.bet = bet;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    // Метод для вычитания суммы со счета
    public void subtractBalance(Float amount) {
        if (balance >= amount) {
            balance -= amount;
        } else {
            throw new IllegalArgumentException("Недостаточно средств на балансе для списания " + amount);
        }
    }

    // Метод для добавления суммы на счет
    public void addBalance(Float amount) {
        if (amount > 0) {
            balance += amount;
        } else {
            throw new IllegalArgumentException("Сумма добавления должна быть положительной");
        }
    }
    // Геттер для name
    public String getName() {
        return name;
    }

    // Сеттер для name
    public void setName(String name) {
        this.name = name;
    }
}
