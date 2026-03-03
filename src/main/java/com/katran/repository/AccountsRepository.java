package com.katran.repository;
import com.katran.model.Accounts;
import com.katran.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, Long> {

    // Метод для поиска всех счетов по userId
    @Query("SELECT a FROM Accounts a WHERE a.userId = :userId")
    List<Accounts> findByUserId(@Param("userId") Long userId);


    @Modifying
    @Query("UPDATE Accounts a SET a.amount = :amount WHERE a.userId = :userId")
    int updateAccountInfo(@Param("amount") float amount,@Param("userId") long userId);
}
