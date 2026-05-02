package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id", nullable = false) private Item item;
    @Column(name = "old_price", nullable = false, precision = 19, scale = 4) private BigDecimal oldPrice;
    @Column(name = "new_price", nullable = false, precision = 19, scale = 4) private BigDecimal newPrice;
    @Column(name = "changed_at", nullable = false) @Builder.Default private Instant changedAt = Instant.now();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "changed_by", nullable = false) private User changedBy;
    @Column(columnDefinition = "TEXT") private String reason;
}
