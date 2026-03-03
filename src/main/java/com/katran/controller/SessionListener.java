package com.katran.controller;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SessionListener implements HttpSessionListener {
    private static final AtomicInteger activeSessions = new AtomicInteger();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        activeSessions.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        activeSessions.decrementAndGet();
    }

    public static int getActiveSessions() {
        return activeSessions.get();
    }


}

