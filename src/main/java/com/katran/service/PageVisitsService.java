package com.katran.service;

import com.katran.model.PageVisit;
import com.katran.repository.PageVisitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageVisitsService {

    @Autowired
    private PageVisitsRepository visitsRepository;

    public void incrementPageVisit(String page) {
        PageVisit visit = visitsRepository.findByPage(page)
                .orElse(new PageVisit(page, 0)); // Использование конструктора
        visit.setVisit_count(visit.getVisit_count() + 1);
        visitsRepository.save(visit);
    }

}

