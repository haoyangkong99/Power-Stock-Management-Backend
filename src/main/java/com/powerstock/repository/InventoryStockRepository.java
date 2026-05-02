package com.powerstock.repository;
import com.powerstock.model.entity.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {
    Optional<InventoryStock> findByItemIdAndLocationId(Long itemId, Long locationId);
    List<InventoryStock> findByItemId(Long itemId);
    List<InventoryStock> findByLocationId(Long locationId);
    List<InventoryStock> findByCurrentQuantityLessThanEqual(Integer quantity);
}
