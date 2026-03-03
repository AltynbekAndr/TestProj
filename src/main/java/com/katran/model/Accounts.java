package com.katran.model;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "accounts")
@Data
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long userId;
    private String accountNumber;
    private String accountType;
    private String customerName;
    private String info;
    private float amount;
}