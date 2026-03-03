package com.katran.repository;
import com.katran.model.GameWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameWinnerRepository extends JpaRepository<GameWinner, Long> {

}
