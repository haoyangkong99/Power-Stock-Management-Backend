package com.powerstock.repository;
import com.powerstock.model.entity.StockTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TransactionRepository extends JpaRepository<StockTransaction, Long> {
    Page<StockTransaction> findByItemId(Long itemId, Pageable pageable);
    Page<StockTransaction> findByLocationToId(Long locationId, Pageable pageable);
    Page<StockTransaction> findByUserId(Long userId, Pageable pageable);
    Page<StockTransaction> findAll(Pageable pageable);
    long countBySyncedFalse();
}
