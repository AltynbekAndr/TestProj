package com.katran.model;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "Stock2")
@Data
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String stock_text;
}