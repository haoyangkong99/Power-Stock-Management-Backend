package com.powerstock.repository;
import com.powerstock.model.entity.ItemUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ItemUnitRepository extends JpaRepository<ItemUnit, Long> {
    List<ItemUnit> findByItemIdAndActiveTrue(Long itemId);
    Optional<ItemUnit> findByItemIdAndPrimaryTrue(Long itemId);
    Optional<ItemUnit> findByItemIdAndUnitId(Long itemId, Long unitId);
    boolean existsByItemIdAndUnitId(Long itemId, Long unitId);
}
