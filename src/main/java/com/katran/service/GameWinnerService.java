package com.katran.service;

import com.katran.model.GameWinner;
import com.katran.repository.GameWinnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameWinnerService {
    @Autowired
    private GameWinnerRepository gameWinnerRepository;

    public GameWinner saveGameWinner(GameWinner gameWinner) {
        return gameWinnerRepository.save(gameWinner);
    }

}
