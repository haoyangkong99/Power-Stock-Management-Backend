package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.UnitRequest;
import com.powerstock.dto.response.UnitResponse;
import com.powerstock.model.entity.Unit;
import com.powerstock.model.entity.UnitConversion;
import com.powerstock.repository.UnitConversionRepository;
import com.powerstock.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {
    @Mock private UnitRepository unitRepository;
    @Mock private UnitConversionRepository conversionRepository;
    @InjectMocks private UnitService unitService;

    @Test
    void shouldCreateBaseUnit() {
        UnitRequest request = new UnitRequest();
        request.setName("Piece"); request.setSymbol("pc");

        when(unitRepository.findByName("Piece")).thenReturn(Optional.empty());
        when(unitRepository.findBySymbol("pc")).thenReturn(Optional.empty());
        when(unitRepository.save(any(Unit.class))).thenAnswer(inv -> { Unit u = inv.getArgument(0); u.setId(1L); return u; });

        UnitResponse response = unitService.createUnit(request);
        assertThat(response.getName()).isEqualTo("Piece");
        assertThat(response.getBaseUnitId()).isNull();
    }

    @Test
    void shouldCreateDerivedUnit() {
        UnitRequest request = new UnitRequest();
        request.setName("Dozen"); request.setSymbol("dz");
        request.setBaseUnitId(1L); request.setConversionFactor(BigDecimal.valueOf(12));

        Unit baseUnit = Unit.builder().name("Piece").symbol("pc").build();
        baseUnit.setId(1L);
        when(unitRepository.findByName("Dozen")).thenReturn(Optional.empty());
        when(unitRepository.findBySymbol("dz")).thenReturn(Optional.empty());
        when(unitRepository.findById(1L)).thenReturn(Optional.of(baseUnit));
        when(unitRepository.save(any(Unit.class))).thenAnswer(inv -> { Unit u = inv.getArgument(0); u.setId(2L); return u; });
        when(conversionRepository.save(any(UnitConversion.class))).thenAnswer(inv -> inv.getArgument(0));

        UnitResponse response = unitService.createUnit(request);
        assertThat(response.getName()).isEqualTo("Dozen");
        assertThat(response.getBaseUnitId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenCreatingDuplicateUnit() {
        UnitRequest request = new UnitRequest();
        request.setName("Piece"); request.setSymbol("pc");
        when(unitRepository.findByName("Piece")).thenReturn(Optional.of(new Unit()));
        assertThatThrownBy(() -> unitService.createUnit(request))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Unit name already exists");
    }

    @Test
    void shouldConvertQuantity() {
        Unit pcUnit = Unit.builder().name("Piece").symbol("pc").conversionFactor(BigDecimal.ONE).build();
        pcUnit.setId(1L);
        Unit dzUnit = Unit.builder().name("Dozen").symbol("dz").conversionFactor(BigDecimal.valueOf(12)).build();
        dzUnit.setId(2L);
        UnitConversion conversion = UnitConversion.builder()
                .fromUnit(dzUnit).toUnit(pcUnit).conversionFactor(BigDecimal.valueOf(12)).build();

        when(conversionRepository.findByFromUnitIdAndToUnitIdAndActiveTrue(2L, 1L)).thenReturn(Optional.of(conversion));

        BigDecimal result = unitService.convertQuantity(BigDecimal.valueOf(5), 2L, 1L);
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(60));
    }
}
