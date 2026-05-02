package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.UnitRequest;
import com.powerstock.dto.response.UnitResponse;
import com.powerstock.model.entity.Unit;
import com.powerstock.model.entity.UnitConversion;
import com.powerstock.repository.UnitConversionRepository;
import com.powerstock.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {
    private final UnitRepository unitRepository;
    private final UnitConversionRepository conversionRepository;

    @Transactional
    public UnitResponse createUnit(UnitRequest request) {
        if (unitRepository.findByName(request.getName()).isPresent())
            throw new BusinessException("DUPLICATE_UNIT", "Unit name already exists: " + request.getName());
        if (unitRepository.findBySymbol(request.getSymbol()).isPresent())
            throw new BusinessException("DUPLICATE_SYMBOL", "Unit symbol already exists: " + request.getSymbol());

        Unit unit = Unit.builder().name(request.getName()).symbol(request.getSymbol())
                .conversionFactor(request.getConversionFactor()).build();

        if (request.getBaseUnitId() != null) {
            Unit baseUnit = unitRepository.findById(request.getBaseUnitId())
                    .orElseThrow(() -> new BusinessException("BASE_UNIT_NOT_FOUND", "Base unit not found"));
            unit.setBaseUnit(baseUnit);
            UnitConversion conversion = UnitConversion.builder()
                    .fromUnit(unit).toUnit(baseUnit).conversionFactor(request.getConversionFactor()).build();
            conversionRepository.save(conversion);
        }
        return toResponse(unitRepository.save(unit));
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> getAllUnits() {
        return unitRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal convertQuantity(BigDecimal quantity, Long fromUnitId, Long toUnitId) {
        if (fromUnitId.equals(toUnitId)) return quantity;
        UnitConversion conversion = conversionRepository.findByFromUnitIdAndToUnitIdAndActiveTrue(fromUnitId, toUnitId)
                .orElseThrow(() -> new BusinessException("CONVERSION_NOT_FOUND",
                        "No conversion found from unit " + fromUnitId + " to unit " + toUnitId));
        return quantity.multiply(conversion.getConversionFactor());
    }

    private UnitResponse toResponse(Unit unit) {
        return UnitResponse.builder()
                .id(unit.getId()).name(unit.getName()).symbol(unit.getSymbol())
                .baseUnitId(unit.getBaseUnit() != null ? unit.getBaseUnit().getId() : null)
                .baseUnitName(unit.getBaseUnit() != null ? unit.getBaseUnit().getName() : null)
                .conversionFactor(unit.getConversionFactor()).createdAt(unit.getCreatedAt()).build();
    }
}
