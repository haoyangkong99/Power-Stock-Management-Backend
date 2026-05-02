package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "unit_conversions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UnitConversion extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "from_unit_id", nullable = false) private Unit fromUnit;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "to_unit_id", nullable = false) private Unit toUnit;
    @Column(name = "conversion_factor", nullable = false, precision = 19, scale = 4) private BigDecimal conversionFactor;
    @Column(name = "is_active", nullable = false) @Builder.Default private Boolean active = true;
}
