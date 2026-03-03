package com.katran.service;

import com.katran.repository.AccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountsRepository accountsRepository;

    public void updateAccount(long userId,float amount) {
        accountsRepository.updateAccountInfo(amount,userId);
    }
}

