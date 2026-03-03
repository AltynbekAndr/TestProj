package com.katran.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PromoCodeService {

    public String generatePromoCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // Генерация кода длиной 8 символов
    }
}
