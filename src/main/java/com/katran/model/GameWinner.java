package com.katran.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_winner")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class GameWinner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String full_name;
    private Float amount;
    private LocalDateTime inpDate;

    @Column(name = "total_amount")
    private Float totalAmount;
}
