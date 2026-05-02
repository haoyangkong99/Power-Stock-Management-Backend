package com.powerstock.repository;
import com.powerstock.model.entity.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    Page<PriceHistory> findByItemIdOrderByChangedAtDesc(Long itemId, Pageable pageable);
    List<PriceHistory> findByItemId(Long itemId);
}
