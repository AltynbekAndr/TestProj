package com.katran.controller;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OnlineUsersCache {
    private static final int BASE_USERS = 10000; // Базовое количество пользователей
    private static final int MAX_VARIATION = 500; // Максимальное отклонение (+/-500)
    private final AtomicInteger onlineUsers = new AtomicInteger();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public OnlineUsersCache() {
        // Инициализация значения и регулярное обновление
        onlineUsers.set(calculateOnlineUsers());
        scheduler.scheduleAtFixedRate(() ->
                        onlineUsers.set(calculateOnlineUsers()),
                10, 10, TimeUnit.MINUTES); // Обновляем каждые 10 минут
    }

    private int calculateOnlineUsers() {
        int activeUsers = SessionListener.getActiveSessions();
        int randomVariation = new Random().nextInt(2 * MAX_VARIATION + 1) - MAX_VARIATION; // Отклонение от -500 до +500
        int adjustedUsers = BASE_USERS + randomVariation; // Генерация числа около 10 000
        return Math.max(adjustedUsers, activeUsers); // Гарантия, что результат >= активных сессий
    }

    public int getOnlineUsers() {
        return onlineUsers.get();
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
