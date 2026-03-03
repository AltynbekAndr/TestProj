package com.katran.model;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "PageVisit")
@Data
public class PageVisit {

    public PageVisit() {

    }
    public PageVisit(String page, int visitCount) {
        this.page = page;
        this.visit_count = visitCount;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String page;
    private int visit_count ;


}