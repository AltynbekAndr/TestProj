package com.katran.repository;
import com.katran.model.SendMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SendMoneyRepository extends JpaRepository<SendMoney, Long> {

}
