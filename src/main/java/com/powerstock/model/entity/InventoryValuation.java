package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_valuations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryValuation extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id", nullable = false) private Item item;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "location_id", nullable = false) private Location location;
    @Column(nullable = false) private Integer quantity;
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4) private BigDecimal unitPrice;
    @Column(name = "total_value", nullable = false, precision = 19, scale = 4) private BigDecimal totalValue;
    @Column(name = "calculated_at", nullable = false) @Builder.Default private Instant calculatedAt = Instant.now();
}
