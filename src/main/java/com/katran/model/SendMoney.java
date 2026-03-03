package com.katran.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SendMoney")
@Data
public class SendMoney {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String swiftCode;
    private String bankAccount;
    private String fullname;
    private String cardNumber;
    private String cryptoAddress;
    private String cryptoType;
    private String additionalInfo;
    private LocalDateTime inpDate;
}