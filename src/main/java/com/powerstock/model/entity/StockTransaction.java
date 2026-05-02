package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import com.powerstock.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockTransaction extends BaseEntity {
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TransactionType type;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id", nullable = false) private Item item;
    @Column(nullable = false) private Integer quantity;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4) @Builder.Default private BigDecimal unitPrice = BigDecimal.ZERO;
    @Column(name = "total_value", nullable = false, precision = 19, scale = 4) @Builder.Default private BigDecimal totalValue = BigDecimal.ZERO;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "location_from") private Location locationFrom;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "location_to", nullable = false) private Location locationTo;
    @Column(nullable = false) @Builder.Default private Boolean synced = false;
}
