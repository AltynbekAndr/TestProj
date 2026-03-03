package com.katran.repository;

import com.katran.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock,Long> {
    @Query("SELECT s FROM Stock s ORDER BY s.id DESC")
    Stock findLatestStock();

}



















