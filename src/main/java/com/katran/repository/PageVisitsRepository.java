package com.katran.repository;
import com.katran.model.PageVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PageVisitsRepository extends JpaRepository<PageVisit, Long> {
    Optional<PageVisit> findByPage(String page);
}
