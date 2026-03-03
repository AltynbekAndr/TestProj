package com.katran.service;
import com.katran.model.Stock;
import com.katran.model.User;
import com.katran.repository.StockRepository;
import com.katran.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;


    public Stock getLastStockRecord() {
        return stockRepository.findLatestStock();
    }
}