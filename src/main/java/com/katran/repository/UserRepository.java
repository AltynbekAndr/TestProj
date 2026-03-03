package com.katran.repository;

import com.katran.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    // Новый метод для поиска пользователя по промокоду
    User findByPromoCode(String promoCode); // Найти пользователя по его промокоду
    Optional<User> findByResetToken(String token);

    @Query("SELECT COUNT(u) FROM User u WHERE u.referralCode = :promoCode")
    long countByReferredByPromoCode(@Param("promoCode") String promoCode);

    @Query("SELECT u FROM User u WHERE u.referralCode = :promoCode")
    List<User> getReferralsByPromoCode(String promoCode);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.amount = :amount WHERE u.id = :userId")
    void updateTotalAmount(@Param("userId") Long userId, @Param("amount") float amount);


}



















