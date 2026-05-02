package com.powerstock.repository;
import com.powerstock.model.entity.UnitConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface UnitConversionRepository extends JpaRepository<UnitConversion, Long> {
    Optional<UnitConversion> findByFromUnitIdAndToUnitIdAndActiveTrue(Long fromUnitId, Long toUnitId);
    List<UnitConversion> findByFromUnitIdAndActiveTrue(Long fromUnitId);
    List<UnitConversion> findByToUnitIdAndActiveTrue(Long toUnitId);
    @Query("SELECT uc FROM UnitConversion uc WHERE uc.active = true AND (uc.fromUnit.id = :unitId OR uc.toUnit.id = :unitId)")
    List<UnitConversion> findConversionsForUnit(@Param("unitId") Long unitId);
}
