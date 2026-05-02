package com.powerstock.repository;
import com.powerstock.model.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findBySku(String sku);
    boolean existsBySku(String sku);
    Page<Item> findAll(Pageable pageable);
    Page<Item> findByCategory(String category, Pageable pageable);
    Page<Item> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku, Pageable pageable);
    List<Item> findByReorderLevelGreaterThanAndReorderLevelIsNotNull(int quantity);
}
