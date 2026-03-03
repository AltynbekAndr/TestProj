package com.katran.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WheelSpinHandler wheelSpinHandler;

    public WebSocketConfig(WheelSpinHandler wheelSpinHandler) {
        this.wheelSpinHandler = wheelSpinHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wheelSpinHandler, "/wheel")
                .setAllowedOrigins("https://localhost:8080")  // Разрешить подключение с клиента на том же порту
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

}