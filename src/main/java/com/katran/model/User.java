package com.katran.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String full_name;
    private String password;
    private String role;
    private String email;
    private String country;
    private Float amount;
    private Float totalAmount;
    private String resetToken;
    private LocalDateTime tokenExpiration;
    private String promoCode;
    private String referralCode;
    private ZonedDateTime inpDate;
    private String psw;
    private String ip;
    private String acc_number;
    private String attempt;


}
