package com.katran.model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "chat_2")
@Data
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long userId;
    private String message;
    private String full_name;
    //
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();
}