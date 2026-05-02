package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "units")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Unit extends BaseEntity {
    @Column(nullable = false, unique = true) private String name;
    @Column(nullable = false, unique = true) private String symbol;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "base_unit_id") private Unit baseUnit;
    @Column(name = "conversion_factor", nullable = false, precision = 19, scale = 4) @Builder.Default private BigDecimal conversionFactor = BigDecimal.ONE;
}
