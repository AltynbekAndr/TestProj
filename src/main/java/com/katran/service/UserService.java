package com.katran.service;
import com.katran.model.User;
import com.katran.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user; // Аутентификация успешна
        }
        return null; // Аутентификация не удалась
    }
    public User save(User user) {
        // Шифруем пароль перед сохранением
        user.setAmount(0f);
        user.setTotalAmount(0f);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByPromoCode(String promoCode) {
        User user = userRepository.findByPromoCode(promoCode);
        if(user!=null){
            return user;
        }
        return null;
    }

    public long countByReferredByPromoCode(String promoCode){
        return userRepository.countByReferredByPromoCode(promoCode);
    }
    public void updateTotalAmount(long userId,  float amount){
        userRepository.updateTotalAmount(userId,amount);
    }
}