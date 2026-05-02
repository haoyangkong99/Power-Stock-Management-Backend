package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Item extends BaseEntity {
    @Column(nullable = false, unique = true) private String sku;
    @Column(nullable = false) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    private String category;
    @Column(name = "base_price", nullable = false, precision = 19, scale = 4) @Builder.Default private BigDecimal basePrice = BigDecimal.ZERO;
    @Column(name = "reorder_level", nullable = false) @Builder.Default private Integer reorderLevel = 0;
    @Column(name = "reorder_quantity", nullable = false) @Builder.Default private Integer reorderQuantity = 0;
}
