package com.powerstock.repository;
import com.powerstock.model.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UnitRepository extends JpaRepository<Unit, Long> {
    Optional<Unit> findBySymbol(String symbol);
    Optional<Unit> findByName(String name);
    List<Unit> findByBaseUnitIdIsNull();
    List<Unit> findByBaseUnitId(Long baseUnitId);
}
