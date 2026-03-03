package com.katran.conf;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class WebSocketSessionManager {
    // Множество для хранения сессий
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    // Добавление сессии
    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    // Удаление сессии
    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    // Отправка сообщения всем сессиям
    public void sendMessageToAll(String message) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}